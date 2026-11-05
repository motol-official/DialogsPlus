================================================================================
 Dialogs+ (DialogsPlus)
 A configurable, vanilla-dialog-based menu plugin for Paper 1.21.11-Latest.
================================================================================

WHAT THIS IS
------------
Dialogs+ lets a server owner build in-game menus using Minecraft's native
Dialog system (added 1.21.6, Paper API since 1.21.7) no client mods or
resource packs required for anyone joining the server. Menus are defined in
YAML config files and can contain buttons, text/number/boolean/option inputs,
and actions like running commands, opening sub-menus, sending messages,
opening a URL, or suggesting a command.

You can write the YAML by hand, or build and edit menus entirely in-game with
the built-in editor (see IN-GAME EDITOR below).

REQUIREMENTS
------------
- Paper 1.21.11 (or a Paper fork built on it)
- Java 25 on the server
- (Optional) PlaceholderAPI, if you want placeholders in titles/body/tooltips


FIRST RUN
---------
On first startup, Dialogs+ creates:
    plugins/DialogsPlus/
        menus/                  <- put your menu .yml files here
        exports/                <- menus you've exported land here
        imports/                <- drop a menu file here before importing it
        backups/                <- created by the editor (see IN-GAME EDITOR)
        config.yml              <- plugin settings, command mapping, messages
        README.txt              <- this file, auto-copied here once

The menus folder starts empty. You can create your first menu in-game with
"/dialogsplus edit" (then "+ New menu"), or add your own menu files by hand
(see EXAMPLE MENU below) and restart the server or run "/dialogsplus reload".

NOTE: README.txt is only copied on first run. After updating the plugin,
delete plugins/DialogsPlus/README.txt and restart to get the newest copy.


COMMANDS
--------
    /dialogs <menu-id>            Opens the given menu for the player running it
    /dialogsplus reload           Reloads all menu files from disk
    /dialogsplus export <menu-id> Copies a menu file into plugins/DialogsPlus/exports/
    /dialogsplus import <file>    Copies a file from plugins/DialogsPlus/imports/
                                   into menus/ and reloads
    /dialogsplus edit             Opens the in-game menu editor (players only)
    /dialogsplus edit <menu-id>   Opens the editor directly on one menu
    Plus any custom commands you've mapped in config.yml (see below)


PERMISSIONS (COMMANDS)
----------------------
    dialogsplus.edit              Use the in-game editor (default: op)
    (reload, export and import use the reload permission set in config.yml)

WARNING: treat dialogsplus.edit like console access. Anyone who has it can
create RUN_COMMAND_AS_CONSOLE actions. Give it to trusted admins only.


IN-GAME EDITOR
--------------
Run "/dialogsplus edit" to open the editor. It is built from the same native
Dialog system as your menus, so it needs no extra mods, ports or web server.

From the menu list you can:
    - Create a new menu ("+ New menu": an ID and a title; it starts with one
      Close button, because a dialog needs at least one button)
    - Open any menu to edit it
    - Delete a menu (with confirmation)

Inside a menu you can edit:
    - Settings: title, external title, permission, can-close-with-escape
    - Body text: one line per message
    - Components: add, edit, reorder (Move up / Move down) and delete, for
      all 5 types (BUTTON, TEXT_INPUT, BOOLEAN_INPUT, NUMBER_INPUT,
      SINGLE_OPTION_INPUT)
    - Actions on buttons: add, edit, reorder and delete, for all 7 action
      types
    - Preview: opens the real menu. Its buttons really run their actions!

Every change is saved to the menu's YAML file immediately. There is no
separate "apply" step and you don't need to run /dialogsplus reload.

Backups and things to know:
    - Saving rewrites the whole YAML file, so COMMENTS IN YOUR FILE ARE LOST.
      The first time the editor saves a menu, your original file is copied to
      plugins/DialogsPlus/backups/<id>.original.yml
    - Deleting a menu keeps a copy in backups/<id>.deleted.yml
    - Don't run /dialogsplus reload while someone is in the middle of editing.
      Reload replaces the loaded menus, and the open editor would then save
      stale data.
    - Not editable in-game yet: renaming a menu ID, changing a component's
      type (delete and re-add it instead), and the "commands:" section of
      config.yml.


COMMAND MAPPING (config.yml)
------------------------------
You can map any custom command straight to a menu, so players don't need to
know a menu's id at all. Set this in plugins/DialogsPlus/config.yml:

    commands:
      main: "/menu"
      tpa-menu: "/teleport"

Typing "/menu" then behaves exactly like "/dialogs main".

IMPORTANT: changes to the "commands" section require a full server restart
to take effect, unlike menu .yml files, which "/dialogsplus reload" picks
up live.


CURRENTLY IMPLEMENTED FEATURES
-------------------------------
- Menus defined in YAML, one file per menu, loaded from plugins/DialogsPlus/menus/
- In-game menu editor (/dialogsplus edit): create, edit, preview and delete
  menus, components and actions without touching YAML
- Button components with label, tooltip, and an optional permission requirement
  (a player without the permission simply won't see that button)
- Input components: TEXT_INPUT, BOOLEAN_INPUT, NUMBER_INPUT, SINGLE_OPTION_INPUT
- {input-id} token substitution: a button's command/message/url can reference
  another component's input value by its id, e.g. "/tpa {target}"
- Actions per button, run in sequence: RUN_COMMAND, RUN_COMMAND_AS_CONSOLE,
  CLOSE, SEND_MESSAGE, OPEN_MENU (sub-menu navigation)
- OPEN_URL and SUGGEST_COMMAND actions:
    * If it is the ONLY action on a button, it fires natively and instantly
      when clicked.
    * If combined with other actions on the same button, it instead sends
      the player a clickable chat message they must click themselves.
- Item preview icons: setting "icon" on a button shows an icon on the button
- Player head icons via "icon: player_head:<name>"
- Dynamic "your own head" icon via "icon: player_head:{self}"
- Per-menu permission requirement (a whole menu can be locked down, not just
  individual buttons within it)
- PlaceholderAPI support in menu titles, external titles, body text, button
  labels, and tooltips (only active if PlaceholderAPI is installed; works
  fine without it, placeholders just won't be replaced)
- Import/export of individual menus as shareable .yml files
- Config-driven custom commands that open a menu directly (e.g. "/menu")
- Config-driven messages (prefix, no-permission, menu-not-found, etc.),
  colorable with '&' codes
- /dialogsplus reload - reloads menus from disk without restarting

================================================================================
 CONFIGURATION REFERENCE
================================================================================

WHERE MENU FILES LIVE
----------------------
    plugins/DialogsPlus/menus/<any-name>.yml

Each .yml file in that folder is loaded as one menu on startup or on
"/dialogsplus reload". The menu's "id" field is what you reference it by
(e.g. in /dialogs <id> or an OPEN_MENU action). Menus created in the editor
are saved as menus/<id>.yml.


MENU-LEVEL FIELDS
------------------
    id: main                      # required. Unique id, used in /dialogs <id>
    title: "Main Menu"            # required. Shown at the top of the dialog
    external-title: "Server Menu" # optional. Used if this menu is opened as a
                                   #   button/link from elsewhere
    can-close-with-escape: true   # optional, default true
    permission: "dialogsplus.staff" # optional. Whole menu is locked unless
                                   #   the player has this permission
    body:                          # optional. Lines of plain text
      - "Welcome!"
      - "Choose an option below."
    components:                    # required. See COMPONENTS below
      - ...

A menu needs at least one BUTTON component, otherwise it can't be opened.


COMPONENTS
----------
Each entry in "components" is either a BUTTON or an input field.

  BUTTON:
    - id: homes                 # required, unique within this menu
      label: "Homes"             # required. Text shown on the button
      tooltip: "View your homes" # optional. Hover text
      icon: GRASS_BLOCK          # optional. See ICONS below
      permission: "homes.use"    # optional. Button is hidden if the player
                                  #   lacks this permission
      type: BUTTON
      actions:                   # required. Run in order, see ACTIONS below
        - ...

  TEXT_INPUT:
    - id: target
      label: "Player Name"
      type: TEXT_INPUT

  BOOLEAN_INPUT:
    - id: confirm
      label: "I agree"
      type: BOOLEAN_INPUT

  NUMBER_INPUT:
    - id: amount
      label: "Amount"
      type: NUMBER_INPUT
      min: 0
      max: 100
      step: 1        # optional
      initial: 0     # optional

  SINGLE_OPTION_INPUT:
    - id: choice
      label: "Pick one"
      type: SINGLE_OPTION_INPUT
      options:
        - "Option A"
        - "Option B"

NOTE: Component ids may use letters, numbers, - and _ (max 64 characters).


ICONS
-----

    icon: GRASS_BLOCK              # any vanilla Material name
    icon: player_head:Notch        # a specific player's head by name
    icon: "player_head:{self}"     # the CLICKING player's own head (dynamic)


PERMISSIONS (MENUS)
--------------------
Two levels of permission gating are supported:

  Per-menu (in config.yml and per menu file):
    - Set "permission:" at the top level of a menu .yml file to lock the
      whole menu. A player without it gets the "no-permission" message
      from config.yml instead of the dialog opening.

  Per-button (inside a component):
    - Set "permission:" on a BUTTON component to hide just that button from
      players who lack it. The rest of the menu still opens normally.


PLACEHOLDERAPI SUPPORT
------------------------
If PlaceholderAPI is installed, placeholders are replaced in:
    - Menu title and external-title
    - Body text lines
    - Button labels
    - Button tooltips

Example:
    title: "Welcome, %player_name%"
    body:
      - "Your balance: %vault_eco_balance%"

If PlaceholderAPI is NOT installed, these fields are shown as plain text
with the placeholder syntax left in (nothing breaks, it just won't resolve).


ACTIONS
-------
Each component's "actions" list runs in order when a button is clicked.

  RUN_COMMAND            runs a command as the clicking player
    - type: RUN_COMMAND
      command: "homes"             # no leading slash

  RUN_COMMAND_AS_CONSOLE  runs a command as console
    - type: RUN_COMMAND_AS_CONSOLE
      command: "give {player} diamond 1"

  CLOSE                   closes the current dialog
    - type: CLOSE

  SEND_MESSAGE             sends a plain chat message
    - type: SEND_MESSAGE
      message: "Request sent!"

  OPEN_MENU                opens another menu by id (sub-menu navigation)
    - type: OPEN_MENU
      target: tpa-menu

  OPEN_URL                 opens a link
    - type: OPEN_URL
      url: "https://example.com"

  SUGGEST_COMMAND           pre-fills the player's chat bar with a command
    - type: SUGGEST_COMMAND
      command: "msg SomePlayer "

NOTE: OPEN_URL and SUGGEST_COMMAND only fire instantly if they are the ONLY
action on that button. If chained with other actions, the player instead
gets a clickable chat message.

(In the editor, a leading "/" on RUN_COMMAND / RUN_COMMAND_AS_CONSOLE is
removed automatically, and OPEN_URL must start with http:// or https://.)


{id} SUBSTITUTION
-------------------
A RUN_COMMAND / RUN_COMMAND_AS_CONSOLE / SEND_MESSAGE / OPEN_URL /
SUGGEST_COMMAND value can reference another component's input value in the
same menu using {that-component-id}:

    components:
      - id: target
        label: "Player Name"
        type: TEXT_INPUT

      - id: send
        label: "Send Request"
        type: BUTTON
        actions:
          - type: CLOSE
          - type: RUN_COMMAND
            command: "tpa {target}"


IMPORT / EXPORT
-----------------
Export a menu to share it with someone else or back it up:
    /dialogsplus export <menu-id>
    -> copies plugins/DialogsPlus/menus/<menu-id>.yml into
       plugins/DialogsPlus/exports/<menu-id>.yml

Import a menu someone shared with you:
    1. Place their .yml file into plugins/DialogsPlus/imports/
    2. Run: /dialogsplus import <filename.yml>
    -> copies it into menus/ and reloads automatically


FULL WORKING EXAMPLE
----------------------
plugins/DialogsPlus/menus/main.yml:

    id: main
    title: "Main Menu"
    body:
      - "Welcome! Choose an option below."
    components:
      - id: homes
        label: "Homes"
        icon: GRASS_BLOCK
        tooltip: "View your homes"
        type: BUTTON
        actions:
          - type: CLOSE
          - type: RUN_COMMAND
            command: "homes"

      - id: tpa
        label: "Teleport Request"
        type: BUTTON
        actions:
          - type: OPEN_MENU
            target: tpa-menu

plugins/DialogsPlus/menus/tpa-menu.yml:

    id: tpa-menu
    title: "Teleport Request"
    components:
      - id: target
        label: "Player Name"
        type: TEXT_INPUT

      - id: send
        label: "Send Request"
        type: BUTTON
        actions:
          - type: CLOSE
          - type: RUN_COMMAND
            command: "tpa {target}"

      - id: back
        label: "Back"
        type: BUTTON
        actions:
          - type: OPEN_MENU
            target: main


ROADMAP (not built yet)
------------------------
- Currently no ideas for the roadmap, but more stuff is coming soon

================================================================================