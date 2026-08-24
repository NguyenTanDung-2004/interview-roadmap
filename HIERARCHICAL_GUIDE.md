# Interview Roadmap - Hierarchical UI Documentation

## 🎯 Overview

The roadmap has been reorganized with a **hierarchical structure** supporting:
- Multiple **Domains** (Database, AI, etc.)
- Multiple **Subdomains** per domain (Transactions, Replication, etc.)
- Multiple **Topics** per subdomain
- **Recursive expansion** - subdomains can be collapsed/expanded

## 📁 Directory Structure

```
interview-roadmap/
├── 01-Database/
│   ├── 01-Transactions/
│   │   ├── ACID.md
│   │   ├── Isolation_Levels.md
│   │   └── Deadlocks_TransactionLogging.md
│   ├── 02-Replication/
│   │   ├── how_mysql_replication_works.md
│   │   ├── common_used_for_replication.md
│   │   ├── Delayed_Replication.md
│   │   ├── Multithreaded_Replication_Modes.md
│   │   ├── Replication_Failover.md
│   │   ├── Global_Transaction_Identifiers.md
│   │   └── semissynchronous_replication.md
│   └── 03-Performance/
│       └── indexing-for-high-performance.md
├── 02-AI/
│   ├── 01-Fundamentals/
│   │   ├── AI_AGI.md
│   │   └── LLM.md
│   ├── 02-Embeddings/
│   │   ├── Embeddings_VectorDBs.md
│   │   └── RAG_AIAgents.md
│   └── 03-Training/
│       ├── Fine_Tuning.md
│       └── Training_Inference.md
├── roadmap.json
├── index.html
├── package.json
└── README.md
```

## 🔧 How to Extend

### Adding a New Subdomain

1. **Create folder**: e.g., `01-Database/04-Security/`
2. **Add markdown files**: e.g., `01-Database/04-Security/encryption.md`
3. **Update roadmap.json**: Add subdomain object to the domain's `subdomains` array

Example:
```json
{
  "id": "security",
  "name": "Security & Encryption",
  "icon": "🔒",
  "color": "#d63031",
  "description": "Database security and encryption strategies",
  "topics": [
    {
      "id": "encryption",
      "title": "Encryption Best Practices",
      "file": "01-Database/04-Security/encryption.md",
      "tags": ["security", "encryption"],
      "difficulty": "advanced"
    }
  ]
}
```

### Adding a New Topic to Existing Subdomain

1. **Create markdown file**: e.g., `01-Database/01-Transactions/MVCCLocking.md`
2. **Update roadmap.json**: Add topic object to the subdomain's `topics` array

```json
{
  "id": "mvcc-locking",
  "title": "MVCC & Locking Mechanisms",
  "file": "01-Database/01-Transactions/MVCCLocking.md",
  "tags": ["transactions", "concurrency"],
  "difficulty": "advanced"
}
```

### Adding a Complete New Domain

1. **Create folder**: e.g., `03-SystemDesign/`
2. **Create subdomains**: e.g., `03-SystemDesign/01-Basics/`, `03-SystemDesign/02-Patterns/`
3. **Add content**: Create markdown files in subdomain folders
4. **Update roadmap.json**: Add complete domain object with subdomains and topics

```json
{
  "id": "system-design",
  "name": "System Design",
  "icon": "🏗️",
  "color": "#9b59b6",
  "description": "Learn system design principles and patterns",
  "subdomains": [
    {
      "id": "basics",
      "name": "Basics",
      "icon": "📚",
      "color": "#9b59b6",
      "description": "Foundational concepts",
      "topics": [
        {
          "id": "cap-theorem",
          "title": "CAP Theorem",
          "file": "03-SystemDesign/01-Basics/CAP_Theorem.md",
          "tags": ["distributed-systems", "fundamentals"],
          "difficulty": "intermediate"
        }
      ]
    }
  ]
}
```

## 🎨 Customization

### Change Domain/Subdomain Colors

Edit `roadmap.json` and update the `color` property:

```json
{
  "id": "domain-id",
  "name": "Domain Name",
  "color": "#hexcolor",  // Change this
  "subdomains": [
    {
      "id": "subdomain-id",
      "color": "#hexcolor",  // Also change this
      // ...
    }
  ]
}
```

The left border of cards will automatically match these colors.

### Change Domain/Subdomain Icons

Edit `roadmap.json` and update the `icon` property with any emoji:

```json
{
  "icon": "📚",  // Change emoji
  "name": "Topic Name"
}
```

Available emoji resources:
- https://emojipedia.org/ - Find any emoji you want
- Common tech icons: 🗄️ 🤖 🔒 ⚡ 📡 🔄 🎓 🔍 🏗️

## 🎯 Key Features

### Collapsible Subdomains
- Click the **▼** arrow to collapse/expand subdomains
- Subdomains automatically expand when searching

### Smart Search
- Search by **title** or **tags**
- Results automatically expand subdomains containing matches
- Works across all domains and subdomains

### Filtering
- **All Topics**: Show everything
- **🗄️ Database**: Show only database topics
- **🤖 AI/LLM**: Show only AI topics
- (Easily extensible to add more domain filters)

### Statistics
- Shows total **Domains**, **Subdomains**, and **Topics**
- Tracks completion progress (for future features)

### Responsive Design
- Mobile-friendly tree navigation
- Works on all screen sizes
- Touch-friendly collapse/expand controls

## 📊 roadmap.json Structure

```json
{
  "roadmap": {
    "title": "Roadmap Title",
    "description": "Description",
    "version": "2.0.0",
    "domains": [
      {
        "id": "unique-domain-id",
        "name": "Domain Display Name",
        "icon": "emoji",
        "color": "#hexcolor",
        "description": "Domain description",
        "subdomains": [
          {
            "id": "unique-subdomain-id",
            "name": "Subdomain Display Name",
            "icon": "emoji",
            "color": "#hexcolor",
            "description": "Subdomain description",
            "topics": [
              {
                "id": "unique-topic-id",
                "title": "Topic Title",
                "file": "path/to/file.md",
                "tags": ["tag1", "tag2"],
                "difficulty": "beginner|intermediate|advanced"
              }
            ]
          }
        ]
      }
    ]
  }
}
```

## 🚀 Running the Roadmap

### Method 1: Direct Browser Open
```bash
# Open index.html in your browser
# File → Open File → select index.html
```

### Method 2: Local Server (Python)
```bash
cd interview-roadmap
python -m http.server 8000
# Visit http://localhost:8000
```

### Method 3: Live Server (VS Code Extension)
```bash
# Right-click index.html → Open with Live Server
```

### Method 4: HTTP Server (Node.js)
```bash
npm install -g http-server
http-server
# Visit http://localhost:8080
```

## 💡 Best Practices

### Content Organization
1. **Keep topics focused** - one main concept per file
2. **Use consistent markdown** - standardize heading levels
3. **Add examples** - code blocks help understanding
4. **Link concepts** - reference related topics

### Tagging Strategy
- Use **lowercase tags** with hyphens: `high-availability`, `data-consistency`
- Reuse tags across topics for better searchability
- Limit to 3-5 tags per topic
- Common tags: `fundamentals`, `performance`, `reliability`, `optimization`

### Difficulty Levels
- **Beginner** - No prerequisites, introductory concepts
- **Intermediate** - Requires basic knowledge of domain
- **Advanced** - Complex topics, assumes solid foundation

### File Organization
- Keep folder structure clean and intuitive
- Use numbered prefixes (01-, 02-) for ordering
- Use descriptive file names matching topic titles
- One markdown file per topic

## 🎨 UI Styling

### CSS Classes Available
- `.domain-card` - Top-level domain container
- `.subdomain-card` - Subdomain container
- `.topic-item` - Individual topic
- `.tag` - Topic tag
- `.difficulty-badge` - Difficulty indicator
- `.viewer-section` - Content display area

### Customizing Colors
Edit CSS variables in `index.html`:
```css
:root {
    --primary-color: #2c3e50;
    --secondary-color: #3498db;
    --accent-color: #e74c3c;
    /* ... other variables ... */
}
```

## 🔍 Troubleshooting

### Topics not loading?
- Verify file paths in `roadmap.json` are correct
- Check browser console (F12) for errors
- Ensure markdown files exist at specified paths

### Markdown not rendering?
- The UI supports basic markdown syntax
- For complex markdown, verify syntax in target file
- Check console for parsing errors

### Subdomains not expanding?
- Check browser console for JavaScript errors
- Verify `roadmap.json` syntax is valid
- Ensure subdomains array is properly formatted

### Search not working?
- Clear browser cache (Ctrl+Shift+Delete)
- Hard refresh (Ctrl+Shift+R)
- Check search box value is being captured

## 📝 Version History

- **v2.0.0** - Added hierarchical subdomains support
  - Recursive domain/subdomain structure
  - Collapsible subdomain sections
  - Improved search with auto-expand
  - Better statistics dashboard

- **v1.0.0** - Initial flat structure
  - Basic domain/topic organization
  - Search and filter functionality
  - Markdown content rendering

## 🤝 Contributing

To add new topics or domains:
1. Create appropriate folder structure
2. Add markdown files with content
3. Update `roadmap.json` with metadata
4. Test in browser (open index.html)
5. Verify search/filter functionality

## 📚 Resources

- [Markdown Cheatsheet](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet)
- [Emojipedia](https://emojipedia.org/)
- [Color Picker](https://htmlcolorcodes.com/)
- [JSON Validator](https://jsonlint.com/)

---

**Last Updated**: 2026-07-29  
**Version**: 2.0.0  
**Status**: ✅ Production Ready
