# Obsidian Platinum

The design system for the edge smart-home dashboard and app. The house runs on a Raspberry Pi, and the interface should feel the same way: quiet, local, and in control. The base is graphite and ivory, the one accent is platinum, and the type pairs a serif for names with a clean sans for everything you operate.

## Principles

1. **One accent, used for "on".** `accent` is platinum in dark mode and ink-black in light mode. It marks what is active: a lit icon tile, a switch, a slider fill, the one primary button. If everything is on, the grid gets brighter. That's the point.
2. **Risk has its own color, and nothing else uses it.** The `risk-*` tokens belong to locks and confirmations only. A normal device never borrows them, so a lock is recognizable from across the room.
3. **Status is never color alone.** Every state pairs color with an icon or dot plus a word.
4. **What you can't use isn't there.** Guests get no switches, sliders or action buttons, only state and a "View only" badge. Offline devices keep their shape but go dashed and dim.
5. **Nothing technical on screen.** No device IDs, node addresses, tokens, hashes or tool names, anywhere. Name things by room and device.

## Color

Two themes from one token set: **Obsidian** (dark, the default) and **Ivory** (light). All text pairs meet WCAG AA (4.5:1) on the surfaces named in each token's note. Control borders and icons meet 3:1.

| Role | Tokens |
|---|---|
| Ground and surfaces | `bg` → `surface` (cards) → `surface-2` (recessed wells) → `surface-3` (selected segment) |
| Lines | `line` (hairline), `line-strong` (on-state card, control outline) |
| Text | `ink`, `ink-2` (secondary), `ink-3` (captions; only on `bg` and `surface`) |
| Accent | `accent`, `on-accent` |
| Risk | `risk`, `risk-fill`, `on-risk`, `risk-soft`, `risk-line` |
| Signals | `ok` / `ok-soft`, `warn` / `warn-soft`, `info` / `info-soft` |
| Utility | `focus`, `scrim` |

`platinum` and `graphite` are brand colors for the cover and marketing. UI code uses `accent`.

## Type

- **Instrument Serif** (`--font-display`) for room names, screen titles and the thermostat setpoint. It has a single weight, so never bold it.
- **Inter** (`--font-sans`) for everything else, at weights 400 and 500 only. Turn on tabular numerals (`font-variant-numeric: tabular-nums`) for any number that updates.
- Load both from Google Fonts: `family=Instrument+Serif&family=Inter:wght@400;500`.
- Scale: display 40 (32 on phones), title 28, numeral 44, heading 17, body 15, label 14, meta 13, caption 12. Nothing smaller than 12.

## Space, radius, elevation

- 4px base: 4, 8, 12, 16, 20, 24, 32, 48. Card padding is 16 on phones and 20 from tablet up. Grid gaps are 16. The minimum side gutter is 16.
- Radius: 8 for chips, 12 for buttons and tiles, 18 for cards, 26 for sheets, pill for switches, badges and tabs.
- Flat by default: hairlines separate things. `shadow-float` is only for things that float: the confirmation sheet, menus and toasts.

## Layout

- **Phone** (under 600): one-column device grid, room tabs across the top, bottom navigation (Home, Chat, Admin for admins).
- **Tablet** (600–1023): two-column grid, room tabs, chat as a full screen.
- **Desktop** (1024 and up): side navigation with rooms, an auto-fill grid of cards at least 250px wide, and chat as a right-hand panel. Admin pages use table rows.
- The chat composer reserves slots for image and voice so they can be added later without shifting anything.

## Iconography

Tabler-style outline icons, 24px grid, 1.75 stroke, round caps. 20px inline, 22px in device tiles, 14px in badges and chips. `components/icons.js` holds the set used here. Device mapping: light → bulb, fan → fan, outlet → plug, lock → lock / lock-open, blinds and curtains → blinds, thermostat → thermo, sensor → gauge.

## Motion

Short and functional: 120ms for presses, 180ms for switches, 240ms for sheets, with ease-out. The only looping animations are the settling pulse and the running-chip spinner. Both stop under `prefers-reduced-motion`.

## Copy

Sentence case. Buttons start with a verb ("Turn all off", "Approve"). An action that leads to a confirmation ends with an ellipsis ("Unlock…"). Errors say what happened and what to do next ("It didn't report its new position in time. Check the blind, then try again."). The UI never says "please" or "successfully".

## Hard rules

1. Locks (and any other risky device) use `LockCard`: risk border, shield label, no switch.
2. Offline devices are dashed, dimmed and not focusable or clickable.
3. Guests never see a control that wouldn't work for them. Render the state and a "View only" badge instead.
4. Never render tokens, hashes, IDs or raw tool calls, including in tooltips, logs and error text.
5. Both themes, AA contrast, and phone, tablet and desktop layouts for every screen.
