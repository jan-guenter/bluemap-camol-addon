function camol_gallery:clear
fill 199 99 199 229 99 217 minecraft:smooth_stone
setblock 202 100 204 minecraft:stone
setblock 206 100 204 minecraft:stone
setblock 210 100 204 minecraft:stone
setblock 214 100 204 minecraft:purple_concrete
setblock 218 100 204 minecraft:red_concrete
setblock 222 100 204 minecraft:stone
setblock 223 100 204 minecraft:stone
setblock 226 100 204 minecraft:stone
tellraw @a [{"text":"Camol gallery built. Run /function camol_gallery:tools, then right-click targets left-to-right with the matching items.","color":"aqua"}]
scoreboard players set #ready camol_gallery 1
