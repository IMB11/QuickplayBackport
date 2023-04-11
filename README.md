# Quickplay Backport

This mod ports the quickplay command line arguments added in 1.20 to earlier versions.

## Usage

Logging has not been included, because it has remote logging stuff, which requires networking stuff, which **will definitely not be cross version** compatible.

Most people want quickplay for these simple arguments, which **are** implemented:

- `--quickPlaySingleplayer "New World"`
- `--quickPlayMultiplayer "localhost:25565"`
- `--quickPlayRealms "1234"`

If multiple arguments are used, the first one will be read and the rest will be ignored.