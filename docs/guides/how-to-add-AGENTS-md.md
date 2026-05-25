# How to Add AGENTS.md to Existing Projects

## Example AI Prompt

To add an `AGENTS.md` to this project, use the following prompt with your AI assistant:

```
Add an AGENTS.md to this project. Follow the process and template in https://github.com/avaje/avaje-metrics/blob/HEAD/docs/guides/how-to-add-AGENTS-md.md. It should guide both AI and human developers to the official avaje-metrics guides, and to any other major framework guides used by the project. Use the latest links and match the style of AGENTS.md in sibling projects if available.
```

---

This guide standardizes the process for adding an `AGENTS.md` to any avaje-metrics (or
similar) project, ensuring both AI and human developers have fast, accurate access to
actionable library-specific instructions.

## Purpose

`AGENTS.md` is a developer/AI agent onboarding file. It:

- points to official step-by-step guides for avaje-metrics and any other major frameworks used
- lists key tasks such as JVM metrics setup, method timing, and exporter integration
- ensures consistency and discoverability for both AI and human contributors

## Steps

1. **Clarify the audience and purpose**
   - Confirm `AGENTS.md` is for developer/AI agent onboarding.
2. **Check for reference `AGENTS.md` files**
   - Look for `AGENTS.md` in sibling projects and align style/content.
3. **Use the template below**
   - Update framework links and project-specific notes as needed.
4. **Review and commit**
   - Get feedback from a maintainer or lead before merging.

## Template

```markdown
# AI Agent Instructions

This project uses [avaje-metrics](https://avaje-metrics.github.io) for metrics collection
and export.

Before performing a library-related task, fetch and follow the relevant guide below.

---

## avaje-metrics

Guide index: https://github.com/avaje/avaje-metrics/tree/HEAD/docs/guides/

Key guides (fetch and follow when performing the relevant task):

- Getting started: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/getting-started.md
- Register JVM metrics: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/register-jvm-metrics.md
- Add method timing: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/add-method-timing.md
- Add OpenTelemetry export: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/add-open-telemetry-export.md
- Add StatsD reporting: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/add-statsd-reporting.md
- Add Ebean metrics: https://raw.githubusercontent.com/avaje/avaje-metrics/HEAD/docs/guides/add-ebean-metrics.md
```

---

- [ ] Audience and purpose clarified
- [ ] Template used and links updated
- [ ] Style matches sibling projects
- [ ] Reviewed by maintainer/lead

---
_Keep this guide up to date as frameworks and best practices evolve._
