// This is an example configuration file for MetaEngine. Despite the extension,
// it is really just JSON with C (/**/) and C++ style (//) comments. I used
// the .js extension to make syntax highlighters happy. MetaEngine does not care
// about the file name extension (or lack thereof) so long as the file is valid
// JSON once the comments are removed.
{
    // The number of engines marked "JUDGE" must equal the number of enginenes
    // marked "RECOMENDER"
    "engines": [
        {
            // "argv" is required
            "argv": ["stockfish"],
            // "roles" is required. It must include at least one of "JUDGE" and
            // "RECOMENDER".
            "roles": ["TIMER", "RECOMENDER"],
            // Options set here will be overridden by options set through UCI
            // TODO: "options" is not yet implemented
            /*
            "options": {
                "Hash": 512,
                "Slow Mover": 100,
                // Options of type "button" should have
                // the special value "PRESS"
                "Clear Hash": "PRESS"
            },*/
            "copies": 1
        },
        {
            "argv": ["stockfish"],
            "roles": ["JUDGE"]
        }/*,
        {
            "argv": ["lczero", "-w weights-251.txt"],
            "dir": "/home/henry/Documents/chess/leela-chess/build8gpu/bin",
            "roles": ["RECOMENDER"],
            "bias": 20
        }
        */
    ]
}
