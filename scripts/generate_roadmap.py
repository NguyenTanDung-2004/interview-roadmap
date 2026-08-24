#!/usr/bin/env python3
"""Generate roadmap.json from the real folder structure.

Expected structure:
- NN-Domain/
  - NN-Subdomain/
    - *.md

The script preserves existing domain/subdomain metadata (icon/color/description)
when ids are stable, and refreshes topic entries from markdown files.
"""

from __future__ import annotations

import argparse
import json
import re
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Any


DEFAULT_DOMAIN_COLORS = [
    "#3498db",
    "#e74c3c",
    "#27ae60",
    "#9b59b6",
    "#f39c12",
    "#16a085",
]

IGNORED_TOP_LEVEL_DIRS = {".git", ".idea", "assets", "scripts", "node_modules", "venv", ".venv"}


def slugify(text: str) -> str:
    text = text.strip().lower()
    text = re.sub(r"[^a-z0-9]+", "-", text)
    return text.strip("-")


def strip_prefix(name: str) -> str:
    # Remove numeric ordering prefixes like 01- or 10_
    return re.sub(r"^\d+[\-_\s]*", "", name).strip()


def humanize_name(name: str) -> str:
    base = strip_prefix(name)
    words = re.split(r"[_\-\s]+", base)

    acronyms = {
        "ai": "AI",
        "agi": "AGI",
        "llm": "LLM",
        "rag": "RAG",
        "gtid": "GTID",
        "acid": "ACID",
        "mysql": "MySQL",
        "db": "DB",
    }

    out = []
    for word in words:
        if not word:
            continue
        lower = word.lower()
        if lower in acronyms:
            out.append(acronyms[lower])
        elif word.isupper() and len(word) <= 5:
            out.append(word)
        else:
            out.append(word.capitalize())

    return " ".join(out)


def guess_difficulty(title: str, rel_path: str) -> str:
    text = f"{title} {rel_path}".lower()
    if any(k in text for k in ["intro", "fundamental", "basic", "common", "overview"]):
        return "beginner"
    if any(k in text for k in ["advanced", "failover", "deadlock", "fine tuning", "multithreaded"]):
        return "advanced"
    return "intermediate"


def topic_tags(title: str, domain_name: str, subdomain_name: str) -> list[str]:
    raw = re.split(r"[^a-zA-Z0-9]+", f"{title} {domain_name} {subdomain_name}")
    tags: list[str] = []
    for token in raw:
        t = token.strip().lower()
        if len(t) < 3:
            continue
        if t in {"and", "for", "with", "the", "from"}:
            continue
        if t not in tags:
            tags.append(t)
    return tags[:6]


@dataclass
class Meta:
    name: str = ""
    icon: str = ""
    color: str = ""
    description: str = ""


def load_existing_metadata(roadmap_path: Path) -> tuple[dict[str, Meta], dict[tuple[str, str], Meta], dict[str, dict[str, Any]]]:
    domain_meta: dict[str, Meta] = {}
    sub_meta: dict[tuple[str, str], Meta] = {}
    topic_meta: dict[str, dict[str, Any]] = {}

    if not roadmap_path.exists():
        return domain_meta, sub_meta, topic_meta

    try:
        data = json.loads(roadmap_path.read_text(encoding="utf-8"))
        domains = data.get("roadmap", {}).get("domains", [])
        for domain in domains:
            d_id = domain.get("id", "")
            if not d_id:
                continue

            domain_meta[d_id] = Meta(
                name=domain.get("name", ""),
                icon=domain.get("icon", ""),
                color=domain.get("color", ""),
                description=domain.get("description", ""),
            )

            for sub in domain.get("subdomains", []):
                s_id = sub.get("id", "")
                if not s_id:
                    continue

                sub_meta[(d_id, s_id)] = Meta(
                    name=sub.get("name", ""),
                    icon=sub.get("icon", ""),
                    color=sub.get("color", ""),
                    description=sub.get("description", ""),
                )

                for topic in sub.get("topics", []):
                    file_path = topic.get("file", "")
                    if not file_path:
                        continue
                    topic_meta[file_path] = {
                        "id": topic.get("id", ""),
                        "title": topic.get("title", ""),
                        "tags": topic.get("tags", []),
                        "difficulty": topic.get("difficulty", "intermediate"),
                    }
    except (json.JSONDecodeError, OSError):
        pass

    return domain_meta, sub_meta, topic_meta


def iter_sorted_dirs(path: Path) -> list[Path]:
    dirs = [p for p in path.iterdir() if p.is_dir() and not p.name.startswith(".")]

    def sort_key(p: Path) -> tuple[int, str]:
        m = re.match(r"^(\d+)", p.name)
        order = int(m.group(1)) if m else 9999
        return order, p.name.lower()

    return sorted(dirs, key=sort_key)


def iter_sorted_topics(path: Path) -> list[Path]:
    files = [p for p in path.iterdir() if p.is_file() and p.suffix.lower() == ".md"]

    def sort_key(p: Path) -> tuple[int, str]:
        m = re.match(r"^(\d+)", p.stem)
        order = int(m.group(1)) if m else 9999
        return order, p.name.lower()

    return sorted(files, key=sort_key)


def auto_domain_dirs(root: Path) -> list[Path]:
    candidates: list[Path] = []
    for d in iter_sorted_dirs(root):
        if d.name in IGNORED_TOP_LEVEL_DIRS:
            continue
        # Include if: has numeric prefix (explicit knowledge domain), OR
        # has subfolders with markdown files inside.
        has_numeric_prefix = bool(re.match(r"^\d+", d.name))
        try:
            children = list(d.iterdir())
        except OSError:
            continue
        has_subdir = any(p.is_dir() for p in children)
        has_markdown = any(p.suffix.lower() == ".md" for p in d.rglob("*") if p.is_file())
        if has_numeric_prefix or (has_subdir and has_markdown):
            candidates.append(d)
    return candidates


def build_roadmap(root: Path, roadmap_title: str, version: str, domain_roots: list[str]) -> dict[str, Any]:
    roadmap_path = root / "roadmap.json"
    domain_meta, sub_meta, topic_meta = load_existing_metadata(roadmap_path)

    domains: list[dict[str, Any]] = []
    color_idx = 0

    target_dirs: list[Path] = []
    if domain_roots:
        target_dirs = [(root / d) for d in domain_roots if (root / d).is_dir()]
    else:
        target_dirs = auto_domain_dirs(root)

    for d_dir in target_dirs:
        domain_id = slugify(strip_prefix(d_dir.name))
        d_meta = domain_meta.get(domain_id, Meta())
        domain_name = d_meta.name or humanize_name(d_dir.name)

        if not d_meta.color:
            d_meta.color = DEFAULT_DOMAIN_COLORS[color_idx % len(DEFAULT_DOMAIN_COLORS)]
        color_idx += 1

        subdomains: list[dict[str, Any]] = []

        # Handle markdown files directly under a domain folder by putting them in
        # a fallback subdomain so they are visible in the UI tree.
        domain_topics = iter_sorted_topics(d_dir)
        if domain_topics:
            fallback_sub_id = "general"
            if any(slugify(strip_prefix(s.name)) == fallback_sub_id for s in iter_sorted_dirs(d_dir)):
                fallback_sub_id = "root-topics"

            fallback_meta = sub_meta.get((domain_id, fallback_sub_id), Meta())
            fallback_name = fallback_meta.name or "General"

            fallback_topics: list[dict[str, Any]] = []
            for md in domain_topics:
                rel_file = md.relative_to(root).as_posix()
                cached = topic_meta.get(rel_file, {})
                title = cached.get("title") or humanize_name(md.stem)
                topic_id = cached.get("id") or slugify(md.stem)
                tags = cached.get("tags") or topic_tags(title, domain_name, fallback_name)
                difficulty = cached.get("difficulty") or guess_difficulty(title, rel_file)

                fallback_topics.append(
                    {
                        "id": topic_id,
                        "title": title,
                        "file": rel_file,
                        "tags": tags,
                        "difficulty": difficulty,
                    }
                )

            subdomains.append(
                {
                    "id": fallback_sub_id,
                    "name": fallback_name,
                    "icon": fallback_meta.icon,
                    "color": fallback_meta.color or d_meta.color,
                    "description": fallback_meta.description,
                    "topics": fallback_topics,
                }
            )

        for s_dir in iter_sorted_dirs(d_dir):
            sub_id = slugify(strip_prefix(s_dir.name))
            s_meta = sub_meta.get((domain_id, sub_id), Meta())
            sub_name = s_meta.name or humanize_name(s_dir.name)

            topics: list[dict[str, Any]] = []
            for md in iter_sorted_topics(s_dir):
                rel_file = md.relative_to(root).as_posix()
                cached = topic_meta.get(rel_file, {})
                title = cached.get("title") or humanize_name(md.stem)
                topic_id = cached.get("id") or slugify(md.stem)
                tags = cached.get("tags") or topic_tags(title, domain_name, sub_name)
                difficulty = cached.get("difficulty") or guess_difficulty(title, rel_file)

                topics.append(
                    {
                        "id": topic_id,
                        "title": title,
                        "file": rel_file,
                        "tags": tags,
                        "difficulty": difficulty,
                    }
                )

            subdomains.append(
                {
                    "id": sub_id,
                    "name": sub_name,
                    "icon": s_meta.icon,
                    "color": s_meta.color or d_meta.color,
                    "description": s_meta.description,
                    "topics": topics,
                }
            )

        domains.append(
            {
                "id": domain_id,
                "name": domain_name,
                "icon": d_meta.icon,
                "color": d_meta.color,
                "description": d_meta.description,
                "subdomains": subdomains,
            }
        )

    return {
        "roadmap": {
            "title": roadmap_title,
            "description": "Auto-generated from folder structure",
            "version": version,
            "domains": domains,
        }
    }


def write_roadmap(output_path: Path, data: dict[str, Any]) -> None:
    output_path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def source_fingerprint(root: Path, domain_roots: list[str]) -> tuple[str, int]:
    # A compact snapshot to detect folder/file changes without extra deps.
    if domain_roots:
        roots = [(root / d) for d in domain_roots if (root / d).is_dir()]
    else:
        roots = auto_domain_dirs(root)

    entries: list[str] = []
    latest_mtime = 0

    for domain_dir in roots:
        for path in sorted(domain_dir.rglob("*")):
            if path.is_dir() or path.suffix.lower() == ".md":
                rel = path.relative_to(root).as_posix()
                entries.append(rel)
                try:
                    mtime = int(path.stat().st_mtime)
                    latest_mtime = max(latest_mtime, mtime)
                except OSError:
                    continue

    return "|".join(entries), latest_mtime


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate roadmap.json from folders")
    parser.add_argument("--root", default=".", help="Project root folder")
    parser.add_argument("--output", default="roadmap.json", help="Output JSON path")
    parser.add_argument("--title", default="Interview Roadmap", help="Roadmap title")
    parser.add_argument("--version", default="2.1.0", help="Roadmap version")
    parser.add_argument(
        "--domains",
        nargs="*",
        default=[],
        help="Optional explicit domain folders (example: 01-Database 02-AI)",
    )
    parser.add_argument("--watch", action="store_true", help="Watch folders and regenerate on changes")
    parser.add_argument("--interval", type=float, default=2.0, help="Watch polling interval in seconds")

    args = parser.parse_args()
    root = Path(args.root).resolve()
    output_path = (root / args.output).resolve()

    def generate_once() -> None:
        data = build_roadmap(root=root, roadmap_title=args.title, version=args.version, domain_roots=args.domains)
        write_roadmap(output_path, data)

        domain_count = len(data["roadmap"]["domains"])
        sub_count = sum(len(d["subdomains"]) for d in data["roadmap"]["domains"])
        topic_count = sum(len(s["topics"]) for d in data["roadmap"]["domains"] for s in d["subdomains"])

        print(f"Generated {output_path}")
        print(f"Domains: {domain_count}, Subdomains: {sub_count}, Topics: {topic_count}")

    generate_once()

    if args.watch:
        print(f"Watching for changes every {args.interval}s. Press Ctrl+C to stop.")
        last_fp = source_fingerprint(root, args.domains)
        try:
            while True:
                time.sleep(max(0.5, args.interval))
                current_fp = source_fingerprint(root, args.domains)
                if current_fp != last_fp:
                    print("Change detected. Regenerating roadmap...")
                    generate_once()
                    last_fp = current_fp
        except KeyboardInterrupt:
            print("Stopped watch mode.")


if __name__ == "__main__":
    main()
