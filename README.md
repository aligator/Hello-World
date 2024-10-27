# HelloWorld Mod

HelloWorld is a Fabric Minecraft mod.  
It simply allows you to welcome all players on join with a message.

It automatically displays a welcome message to the players joining your server.
It supports the minecraft formatting codes as well as links in markdown-style.

You can use it to display rules, links or other useful information.

## Configuration

The welcome message can be customized in the `hello_world_on_join.txt` configuration file:

1. **Locate the File**: Once the server is started for the first time with the mod, a file named
   `hello_world_on_join.txt` will be generated in the server's configuration folder.
2. **Edit the File**: Open `hello_world_on_join.txt` in any text editor and customize the welcome message.
3. **Save Changes**: After editing the message, use the `/welcome reload` command in-game to reload the message without
   restarting the server.

### Formatting Options

- **[Color Codes](https://www.digminecraft.com/lists/color_list_pc.php)**: Use `§` followed by a color code (`0-9`,
  `a-f`) to change the text color.
- **[Modifiers](https://www.digminecraft.com/lists/color_list_pc.php)**: Use `§` followed by a modifier code (`k-o`,
  `r`) to add effects like bold, italic, and reset.
- **Clickable Links**: Use Markdown-style link syntax (e.g., `[text](url)`) to add clickable URLs.

## Usage

### Commands

- **`/helloworld show`**: Displays the current welcome message.
- **`/helloworld reload`**: Reloads the welcome message from the configuration file (`hello_world_on_join.txt`).

## Example Configuration

`hello_world_on_join.txt`:

```markdown
§aWelcome to the server, §lAdventurer§r!

§n§b[Click here for our rules](http://some-rules.com)§r

§9Have Fun!
```

## Permissions

You can configure the permissions of this mod according to the following table.  
If no permission manager is installed, it falls back to the default.

| Permission                                        | default \| without permission manager | description                                      |
|---------------------------------------------------|---------------------------------------|--------------------------------------------------|
| dev.aligator.helloworld.action.show               | all players                           | display the message on join                      |
| dev.aligator.helloworld.command.helloworld.show   | all players                           | display the message on command `helloworld show` |
| dev.aligator.helloworld.command.helloworld.reload | op only                               | reload the configuration file                    |