# Donut Thanks (Fabric, Minecraft 1.21.4)

Client-side mod. When DonutSMP tells you `Name paid you $5M`, a full-screen animation with YOUR skin plays.

| Paid | Animation |
|------|-----------|
| 5M   | "Thank you for the gamble!" - wave of your skin sweeping the screen |
| 10M  | Big Spender - skins + confetti rain |
| 20M  | Absolute Legend - giant skin with two orbiting rings of skins |
| 50M  | Whale Alert - ocean swell, skins surfing, splashes |
| 100M | 100 Million Club - fireworks show, skin crowd jumping |
| 500M | Half a Billion - spinning gold skin, glow, coin rain |
| 1B   | Billionaire - rocket launch, flash, firework blow-out |
| 10B  | Mega Billionaire - rainbow tunnel, skins flying at you |
| 100B | God Tier - thunder, aura rings, firework storm, skin army |

Commands: `/donutthanks test 50m`, `/donutthanks toggle`, `/donutthanks stop`

## Build
Needs JDK 21. In this folder: `gradle wrapper --gradle-version 8.12` once, then `./gradlew build`.
Jar: `build/libs/donutthanks-1.0.0.jar` -> drop in `.minecraft/mods` with Fabric Loader + Fabric API 1.21.4.

No JDK? Push this folder to a GitHub repo; Actions builds it and you download the jar from the run's Artifacts.

## If your payment message looks different
Edit the `PAY` regex in `DonutThanksClient.java`.
