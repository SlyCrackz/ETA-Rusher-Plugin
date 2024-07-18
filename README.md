# ETA Plugin for RusherHack

ETA is a plugin for the Rusher client that allows players to set a destination and receive periodic notifications about the estimated time of arrival (ETA) based on their current speed and direction.

It also has a HUD!

## Commands

### `*eta set <x> <z>`
Sets the destination coordinates.

**Example:**
*eta set 1000 500*

Output:
Destination set to (1000, 500).

### `*eta stop`
Stops the ETA calculation and notifications.

**Example:**
*eta stop*

Output:
ETA calculation stopped.

### `*eta eta`
Shows the eta.

**Example:**
*eta eta*

### `*eta setInterval <interval>`
Sets the interval for ETA notifications in seconds. Set to 0 to turn off notifications.

**Example:**
*eta setInterval 60*

Output:
Notification interval set to 60 seconds.

**Example to turn off notifications:**
*eta setInterval 0*

Output:
Notifications turned off.


