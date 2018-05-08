// This is an example configuration file for MetaEngine. Despite the extension,
// it is really just JSON with C (/**/) and C++ style (//) comments. I used
// the .js extension to make syntax highlighters happy. MetaEngine does not care
// about the file name extension (or lack thereof) so long as the file is valid
// JSON once the comments are removed.
{
    "engines": [
        {
            "argv": ["stockfish"],
            "roles": ["TIMER", "JUDGE", "RECOMENDER"],
            "copies": 2
        },
        {
            "argv": ["stockfish"],
            "roles": ["JUDGE"]
        },
        {
            "argv": ["lczero", "-w weights-251.txt"],
            "dir": "/home/henry/Documents/chess/leela-chess/build8gpu/bin",
            "roles": "recomender",
            "bias": 20
        }
    ]
}
