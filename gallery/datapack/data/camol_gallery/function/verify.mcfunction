execute unless score #ready camol_gallery matches 1 run tellraw @s [{"text":"Camol gallery has not been built.","color":"red"}]
execute if score #ready camol_gallery matches 1 run tellraw @s [{"text":"Host gallery exists. Camouflage attachment values require the tuned-item right clicks.","color":"green"}]
