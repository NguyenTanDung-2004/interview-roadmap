# Interview Roadmap - Interactive Learning Platform

A hierarchical, scalable learning roadmap with interactive UI for organizing and visualizing educational content.

## 🎯 Features

- **Hierarchical Structure**: Domains → Subdomains → Topics
- **Interactive UI**: Collapsible sections with smooth animations
- **Smart Search**: Search by title or tags across all content
- **Filtering**: Filter by domain or show all topics
- **Responsive Design**: Works on desktop and mobile
- **File-Based**: No backend required, all content in markdown
- **Fully Customizable**: Add new domains, subdomains, and topics easily
- **Statistics Dashboard**: Track total domains, subdomains, and topics

## 🚀 Quick Start

### Option 1: Open in Browser
Simply double-click `index.html` or open it with your browser

### Option 2: Local Server
```bash
# Python
python -m http.server 8000
# Then visit http://localhost:8000

# Or Node.js
npm install -g http-server && http-server
```

### Option 3: VS Code Live Server
Right-click `index.html` → "Open with Live Server"

### Option 4: Auto-build Roadmap From Real Folders
```bash
# Generate roadmap.json once from folder structure
python scripts/generate_roadmap.py

# Watch mode: regenerate whenever folders/files change
python scripts/generate_roadmap.py --watch
```

## 📁 Structure

```
interview-roadmap/
├── 01-Database/              # Domain folder
│   ├── 01-Transactions/      # Subdomain
│   ├── 02-Replication/       # Subdomain
│   └── 03-Performance/       # Subdomain
├── 02-AI/                    # Domain folder
│   ├── 01-Fundamentals/      # Subdomain
│   ├── 02-Embeddings/        # Subdomain
│   └── 03-Training/          # Subdomain
├── index.html               # Main UI
├── roadmap.json             # Configuration & metadata
└── HIERARCHICAL_GUIDE.md    # Detailed documentation
```

## 📊 Current Content

### Database & MySQL (12 topics)
- **Transactions & ACID** (3 topics)
  - ACID Properties
  - Isolation Levels
  - Deadlocks & Transaction Logging
- **Replication & High Availability** (7 topics)
  - How MySQL Replication Works
  - Multithreaded Replication Modes
  - Semi-Synchronous Replication
  - Delayed Replication
  - Replication Failover
  - Global Transaction Identifiers
  - Common Uses for Replication
- **Performance & Optimization** (1 topic)
  - Indexing for High Performance

### AI & LLM (6 topics)
- **Fundamentals** (2 topics)
  - AI & AGI Fundamentals
  - Large Language Models (LLM)
- **Embeddings & Retrieval** (2 topics)
  - Embeddings & Vector Databases
  - RAG & AI Agents
- **Training & Fine-tuning** (2 topics)
  - Fine-Tuning LLMs
  - Training & Inference

## 🎨 Customization

### Add New Subdomain
1. Create folder: `01-Database/04-NewSubdomain/`
2. Add markdown files
3. Run: `python scripts/generate_roadmap.py`

### Add New Topic
1. Create markdown file in subdomain folder
2. Run: `python scripts/generate_roadmap.py`

### Add New Domain
1. Create domain folder: `03-NewDomain/`
2. Create subdomains inside
3. Add markdown files
4. Run: `python scripts/generate_roadmap.py`

See [HIERARCHICAL_GUIDE.md](HIERARCHICAL_GUIDE.md) for detailed examples.

## 🔧 Configuration

Edit `roadmap.json` to:
- Add/remove domains, subdomains, and topics
- Change colors and icons
- Update descriptions and tags
- Modify difficulty levels

Example domain structure:
```json
{
  "id": "domain-id",
  "name": "Domain Name",
  "icon": "📚",
  "color": "#hexcolor",
  "description": "Description",
  "subdomains": [
    {
      "id": "subdomain-id",
      "name": "Subdomain Name",
      "icon": "📖",
      "color": "#hexcolor",
      "description": "Description",
      "topics": [
        {
          "id": "topic-id",
          "title": "Topic Title",
          "file": "path/to/file.md",
          "tags": ["tag1", "tag2"],
          "difficulty": "intermediate"
        }
      ]
    }
  ]
}
```

## 📚 Supported Markdown Features

- Headers (h1-h6)
- **Bold** and *italic*
- [Links](url)
- `Code` and code blocks
- Lists (bullet and numbered)
- Line breaks and paragraphs

## 🎯 UI Features

### Search
- Search by title or tags
- Auto-expands subdomains with matches
- Real-time filtering

### Filtering
- All Topics
- By Domain (Database, AI)
- Easily extensible to add more filters

### Navigation
- Click topics to view content
- Collapse/expand subdomains
- Sticky content viewer on desktop
- Smooth animations and transitions

## 📈 Future Enhancements

- [ ] Progress tracking (mark topics as completed)
- [ ] Bookmarking system
- [ ] Export as PDF
- [ ] Dark mode toggle
- [ ] Time estimates per topic
- [ ] Dependencies/prerequisites visualization
- [ ] Quiz/assessment features
- [ ] Discussion comments
- [ ] Multi-language support

## 💾 Tech Stack

- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Data**: JSON (metadata), Markdown (content)
- **Hosting**: Static files (no backend needed)
- **Browser**: All modern browsers (Chrome, Firefox, Safari, Edge)

## 📝 How to Contribute

1. Add markdown content to appropriate subdomain folder
2. Update `roadmap.json` with topic metadata
3. Test in browser (refresh index.html)
4. Verify search and filtering work correctly

## 🎓 Content Guidelines

- **Keep it concise**: Focus on key concepts
- **Use examples**: Add code snippets where relevant
- **Be consistent**: Use same markdown style across topics
- **Add context**: Explain why topics matter
- **Link topics**: Reference related concepts

## 🐛 Troubleshooting

### Content not loading
- Check file paths in `roadmap.json`
- Verify markdown files exist
- Check browser console for errors

### Search not working
- Clear browser cache
- Verify search query is in title or tags
- Check `roadmap.json` syntax

### UI not updating
- Hard refresh browser (Ctrl+Shift+R)
- Check for JavaScript errors in console
- Verify JSON is valid (use jsonlint.com)

## 📖 Documentation

- [HIERARCHICAL_GUIDE.md](HIERARCHICAL_GUIDE.md) - Comprehensive guide for extending the roadmap
- [roadmap.json](roadmap.json) - Current content structure
- [index.html](index.html) - UI source code with inline documentation

## 📄 License

MIT License - Use freely for educational purposes

## 👤 Author

Created for interview preparation and learning roadmap organization

---

**Last Updated**: 2026-07-29  
**Version**: 2.0.0 (Hierarchical with Subdomains)  
**Status**: ✅ Ready to Use
