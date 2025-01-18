------------------------------------------------------
Create 6.0.0
------------------------------------------------------
Additions
- Example

Changes
- Bumped the defalt max rope length to 384
- Set vault capacity limit to 2048 slots to prevent people from OOM-ing themselves if they set the capacity too high

Bug Fixes
- Fix certain blocks messing up the order scheduled ticks (#7141)
- Fix unbreakable superglue not being usable (#6253)
- Fix update suppression (#7176)
- Fix comparator output of depots ignoring the items max stack size (#7179)
- Fix deployers retaining the damage attribute of their last held weapon (#4870)
- Fix an exploit allowing people to create clipboards that execute commands (#7218)
- Fix redstone links not updating their redstone output when they've been taken out of receiver mode (#7226)
- Fix rare crash related to sliding doors (#6184)
- Verify that schematics are gzip-encoded before trying to read from them (#6087)
- Added workaround for create_tracks.dat getting corrupted during crashes, the mod will try to restore the old track data stored in the create_tracks.dat_old file if the current one is corrupted
- Fix contraptions triggering pressure plates and tripwires (#7255)
- Fix ConditionContext nbt in trains containing a large number of empty tags
- Fix deployers not placing fish from fish buckets (#3705)
- Fix gasses not being visible in basins and item drains (#7236)

Art Changes
- Example

API Changes
- Removed LangMerger and related classes
- Implemented an api to allow mods to register schematic requirements, partial safe nbt and contraption transforms without implementing interfaces (#4702)
- Add a method that developers can override to change the icon in goggle tooltips
- Refactored Item Attributes types, Fan processing types and Arm interaction points, all 3 now use proper registries
