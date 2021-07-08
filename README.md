# Tob Mistake Tracker

Tracks mistakes made by players throughout the Theatre of Blood.

---
This plugin will track mistakes for you and your teammates in the Theatre of Blood. It will also track mistakes for
other raiders while you're spectating.

By default, when detecting a mistake, all players with this plugin will receive a public message of the mistake, a
chat overhead above the player who made the mistake, and the mistake will be added to the Tob Mistake Tracker
side-panel.

Current mistakes being tracked:

* ![death](src/main/resources/com/tobmistaketracker/death.png) **Deaths** throughout the raid (including specific death counts per room)
* ![maiden_blood](src/main/resources/com/tobmistaketracker/maiden_blood.png) Standing in **Maiden** Blood
* ![bloat_hand](src/main/resources/com/tobmistaketracker/bloat_hand.png) Getting hit by **Bloat** Hands
* ![verzik_p2_bounce](src/main/resources/com/tobmistaketracker/verzik_p2_bounce.png) Getting bounced during **Verzik P2**
* ![verzik_p2_bomb](src/main/resources/com/tobmistaketracker/verzik_p2_bomb.png) Getting bombed during **Verzik P2**
* ![verzik_p2_acid](src/main/resources/com/tobmistaketracker/verzik_p2_acid.png) Stepping on acid during **Verzik P2**
* ![verzik_p3_web](src/main/resources/com/tobmistaketracker/verzik_p3_web.png) Getting webbed during **Verzik P3**
* ![verzik_p3_purple](src/main/resources/com/tobmistaketracker/verzik_p3_purple.png) Taking a purple tornado during **Verzik P3**

Coming Soon:

* ![sot_mage_orb](src/main/resources/com/tobmistaketracker/sot_mage_orb.png) Taking damage from a **Soteseg** orb with no vengeance
* ![verzik_p3_melee](src/main/resources/com/tobmistaketracker/verzik_p3_melee.png) Meleeing the team as the tank during **Verzik P3**
* Other feature requests

---

## Screenshots

![panel](src/main/resources/com/tobmistaketracker/panel_action.png)

![death](src/main/resources/com/tobmistaketracker/death_action.png)

![maiden_blood](src/main/resources/com/tobmistaketracker/maiden_blood_action.png)

---

## Changes

#### 2.1
* Add config for toggling mistake overhead text

#### 2.0
* Track raid counts for each player
* Track deaths per boss rom
* Add death grouping with new death icons per boss room to panel
* Fix chat overhead messages not always displaying long enough
* Reorder boxes in panel to have local player first followed by current raiders

#### 1.0
* Initial release