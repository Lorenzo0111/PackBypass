# PackBypass

A clientside mod to bypass resourcepack server requirement

## How to use
1. Download the latest release from [here](https://github.com/Lorenzo0111/PackBypass/releases)
2. Put the jar file in your mods folder
3. Launch the game
4. Deny the resourcepack prompt
5. Enjoy

## How it works
This mod works by altering the `ResourcePackStatusS2CPacket` packet to always return `SUCCESS` instead of `DECLINED` when the server requests the client to download a resourcepack.

## License
This mod is licensed under the MIT License. You can find the license [here](LICENSE)