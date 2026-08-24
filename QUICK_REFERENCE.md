# Quick Reference - Common Tasks

## ⚡ Quick Operations

### Open the Roadmap
```bash
# 1. Double-click index.html
# OR
# 2. Python: python -m http.server 8000
# OR  
# 3. Node: npx http-server
```

### Search & Filter
- **Search box** (top): Type to search by title or tags
- **Buttons** (top): Click "Database" or "AI" to filter by domain
- **Collapse** (▼): Click arrow next to subdomain to collapse/expand

---

## 📋 Add New Topic (Easiest Task)

### Step 1: Create markdown file
Create file: `01-Database/01-Transactions/NewTopic.md`

```markdown
# Topic Title

## Introduction
Write your content here...

## Key Points
- Point 1
- Point 2

## Conclusion
Summary...
```

### Step 2: Update roadmap.json
Add to appropriate subdomain's topics array:

```json
{
  "id": "new-topic",
  "title": "New Topic Title",
  "file": "01-Database/01-Transactions/NewTopic.md",
  "tags": ["tag1", "tag2"],
  "difficulty": "intermediate"
}
```

### Step 3: Refresh browser
- Clear cache: Ctrl+Shift+Delete
- Hard refresh: Ctrl+Shift+R
- Done! ✅

---

## 📁 Add New Subdomain (Medium Task)

### Step 1: Create folder structure
```
01-Database/04-NewSubdomain/
├── file1.md
├── file2.md
└── file3.md
```

### Step 2: Update roadmap.json
Add to domain's subdomains array:

```json
{
  "id": "new-subdomain",
  "name": "New Subdomain Name",
  "icon": "📚",
  "color": "#hexcolor",
  "description": "Description of subdomain",
  "topics": [
    {
      "id": "topic1",
      "title": "Topic 1",
      "file": "01-Database/04-NewSubdomain/file1.md",
      "tags": ["tag1"],
      "difficulty": "intermediate"
    }
  ]
}
```

### Step 3: Refresh browser
Done! ✅

---

## 🏗️ Add New Domain (Advanced Task)

### Step 1: Create folder structure
```
03-SystemDesign/
├── 01-Basics/
│   ├── topic1.md
│   └── topic2.md
├── 02-Patterns/
│   └── topic3.md
```

### Step 2: Update roadmap.json
Add to root domains array:

```json
{
  "id": "system-design",
  "name": "System Design",
  "icon": "🏗️",
  "color": "#9b59b6",
  "description": "Learn system design",
  "subdomains": [
    {
      "id": "basics",
      "name": "Basics",
      "icon": "📚",
      "color": "#9b59b6",
      "description": "Fundamentals",
      "topics": [
        {
          "id": "cap-theorem",
          "title": "CAP Theorem",
          "file": "03-SystemDesign/01-Basics/CAP_Theorem.md",
          "tags": ["distributed-systems"],
          "difficulty": "intermediate"
        }
      ]
    }
  ]
}
```

### Step 3: Refresh browser
Done! ✅

---

## 🎨 Common Customizations

### Change Colors
Edit `color` in roadmap.json:
```json
"color": "#e74c3c"  // Red
"color": "#27ae60"  // Green
"color": "#3498db"  // Blue
"color": "#f39c12"  // Orange
"color": "#9b59b6"  // Purple
"color": "#16a085"  // Teal
```

### Change Icons
Use any emoji, common tech icons:
- 📚 Books (fundamentals)
- 🗄️ Database
- 🤖 AI/Robots
- ⚡ Performance
- 🔒 Security
- 📡 Replication
- 🔍 Search
- 🎓 Education
- 🏗️ Architecture

### Change Difficulty
Options: `"beginner"`, `"intermediate"`, `"advanced"`

### Add Tags
Keep tags lowercase with hyphens:
```json
"tags": ["high-availability", "replication", "performance"]
```

---

## ✅ Validation Checklist

After making changes:

- [ ] All markdown files exist at specified paths
- [ ] `roadmap.json` is valid (use jsonlint.com)
- [ ] File paths use `/` (not `\`)
- [ ] All required fields present (id, title, file, tags, difficulty)
- [ ] Tags are lowercase
- [ ] IDs are unique within their scope
- [ ] Icons are valid emoji
- [ ] Colors are valid hex codes
- [ ] Browser cache cleared
- [ ] Page hard-refreshed (Ctrl+Shift+R)

---

## 🔍 Find & Replace Tips

### Validate JSON Syntax
```
Online: https://jsonlint.com/
Paste roadmap.json content → Click "Validate"
```

### Find file paths
```powershell
# Find all markdown files
Get-ChildItem -Recurse -Filter "*.md"

# Find in specific folder
Get-ChildItem -Path "01-Database" -Recurse -Filter "*.md"
```

### Quick path check
```powershell
# Test if file exists
Test-Path "01-Database/01-Transactions/ACID.md"
```

---

## 🎯 Common Errors & Fixes

### Error: "Cannot find file at path"
- Check path spelling exactly (case-sensitive on Mac/Linux)
- Use `/` not `\` in paths
- Ensure file exists: `Test-Path "path/to/file.md"`

### Error: Search not working
- Hard refresh browser (Ctrl+Shift+R)
- Clear cache completely
- Check tags are in lowercase

### Error: Subdomain won't expand
- Check JSON syntax is valid
- Verify `subdomains` array is properly formatted
- Ensure topics array exists

### Error: Topics not showing
- Verify `roadmap.json` paths are correct
- Check file exists in folder
- Look at browser console for errors (F12)

---

## 📚 File Path Examples

All paths in `roadmap.json` should use forward slashes `/`:

✅ Correct:
```
"file": "01-Database/01-Transactions/ACID.md"
"file": "02-AI/01-Fundamentals/LLM.md"
```

❌ Wrong:
```
"file": "01-Database\01-Transactions\ACID.md"  // Backslashes
"file": "./01-Database/ACID.md"  // Relative paths
"file": "/01-Database/ACID.md"   // Leading slash
```

---

## 🚀 Performance Tips

- Keep markdown files under 10MB
- Limit tags to 3-5 per topic
- Use simple markdown (no complex HTML)
- Organize files in logical folders
- Don't nest domains too deeply (max 3 levels)

---

## 📞 Need Help?

1. **Check documentation**: Read [HIERARCHICAL_GUIDE.md](HIERARCHICAL_GUIDE.md)
2. **Validate JSON**: Use https://jsonlint.com/
3. **Test file paths**: Use `Test-Path` command
4. **Check console**: Press F12 in browser, look for errors
5. **Clear cache**: Ctrl+Shift+Delete, then Ctrl+Shift+R

---

**Last Updated**: 2026-07-29  
**Quick Reference v1.0**
