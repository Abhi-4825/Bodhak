# Security Policy

## Offline-First & Privacy-First

Anuviya is designed with a strict offline-first and privacy-first architecture. 

- **No Source Code Uploads:** Your raw source code is never uploaded to any external servers. All processing and analysis happen locally on your machine.
- **Local AI:** When using the AI analysis features, Anuviya communicates exclusively with your locally running Ollama instance (typically `localhost:11434`). We send only verified compiler facts and structural summaries to the local AI model, preserving the confidentiality of your implementation details.
- **Local Storage:** All workspace data, session information, and analysis caches are stored securely on your local file system, typically at `~/.anuviya/workspace` (directory name retained for backwards compatibility).

## Supported Versions

We provide security updates for the following versions:

| Version | Supported          |
| ------- | ------------------ |
| 0.5.x   | :white_check_mark: |
| 0.4.x   | :white_check_mark: |
| < 0.4   | :x:                |

## Reporting a Vulnerability

If you discover a security vulnerability within Anuviya, please do not disclose it publicly. Instead, please report it via email to:

**abhisekr18j@gmail.com**

Please include:
- A description of the vulnerability.
- Steps to reproduce the issue.
- Potential impact.

We will acknowledge receipt of your report within 48 hours and provide updates as we investigate and develop a patch.
