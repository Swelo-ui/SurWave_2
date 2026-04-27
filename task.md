# Cherry-pick Updates Tasks

- `[x]` Abort current cherry-pick on `main`
- `[x]` Create and switch to new branch `chore/upstream-updates`

## Batch 1 — UI/Behavior Fixes
*(Skipped already cherry-picked: d8365706, c297f35f, 40254759)*
- `[x]` `3eb328c8` — Improve search focus & navigation
- `[x]` `2f02520f` — Navigate to album on song title click
- `[x]` `750afd77` — MiniPlayer background styles + playlist button
- `[x]` `de7b9a26` — Speed dial grid for tablets/large screens
- `[x]` `4fff9f07` — Fix scroll-to-top on nav bar click

## Batch 2 — More Bug Fixes + Lyrics
- `[x]` `a197ca57` — MiniPlayer colors fix (Gradient/Blur/Pure Black)
- `[x]` `e6b2a2de` — TextFieldDialog crash + duplicate key errors
- `[x]` `831ece39` — Ghost adds on playlists fix
- `[x]` `e13a34e0` — Lyrics pipeline overhaul [experimental]
- `[x]` `b1fd2d2f` — Chore cleanup
- `[ ]` `2426919f` — Update dependencies and gradle

## Batch 3 — Playback & Cache Fixes
- `[x]` `9cfff7fe` — Hide lyrics provider banner for manual lyrics
- `[x]` `535246ac` — Play music from URL in search bar
- `[x]` `00119908` — Fix cached/downloaded song labels
- `[x]` `0cee3ad5` — Fix gradle warnings

## Batch 4 — Playlist & Stability
- `[x]` `3bb60ad3` — Fallback for empty radio queues
- `[x]` `1e0a3ec3` — Stop music when task cleared (onTaskRemoved)
- `[x]` `4ef332d3` — Songs persist in playlist across sync
- `[x]` `254eef4a` — Fix LyricsPlus parser

## Batch 5 — Play Queue & Download
- `[x]` `40055b7f` — Update YouTube Extractor
- `[x]` `3b24f274` — Fix play-next + ghost adds
- `[x]` `31b4d7f7` — Update DB when download removed
- `[x]` `1d1a1054` — Handle back gesture in expanded player

## Batch 6 — Sync & Service
- `[x]` `cd723091` — Preserve downloaded songs in playlist sync
- `[x]` `ac7020a8` — Use startForegroundService for background-safe startup
- `[x]` `bbc0350d` — Library search in library screens
- `[x]` `21b37423` — Validate song existence before adding to playlist
- `[x]` `4eb74a16` — Lyrics providers timeout fix

## Batch 7 — Speed & Lyrics Features
- `[x]` `7f2cd433` — Varispeed: link pitch and speed
- `[x]` `cef3566b` — Scroll to current song on shuffle toggle
- `[x]` `93ccf89e` — Update cipher obfuscation (YouTube fix)
- `[x]` `2cb76e16` — AM Lyrics caching API (reduce API load)
- `[x]` `a404748f` — Prefetch lyrics for next song

## Batch 8 — Quality + New Features
- `[x]` `def9d199` — Very high audio quality + autoplay setting
- `[x]` `b4508848` — Android Auto song artworks fix
- `[x]` `495423c5` — Playlist create button in library tab
- `[x]` `003d7024` — Listen Together desync + session fixes
- `[x]` `395c0770` — Listen Together race condition, ANR, DataStore IO

## Batch 9 — Performance + Final Fixes
- `[ ]` `dadc5bbd` — collectAsStateWithLifecycle migration (battery/memory) [SKIPPED - Incompatible with SurWave]
- `[x]` `639b4782` — Gemini voice play fix
- `[x]` `76054465` — Multiple lyric fixes (word-level sync, BetterLyrics, gap indicator)
