# Incendiary API — Side

```java
package dev.incendiary.api;

public enum Side { CLIENT, SERVER, BOTH }
```

`client` runs only with the client, `server` only on a dedicated server,
`both` everywhere (default). A mod loaded on the wrong side is skipped at
discovery, not crashed — it simply never initializes.

[← Back to index](README.md)
