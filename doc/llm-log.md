# LLM Interaction Log (Project 1b & 1c Imperative & Functional TopWords)

## Tool used
ChatGPT (OpenAI)

## What I asked for help with
- Converting the Project 1b imperative solution into a purely functional Scala 3 solution.
- Designing a sliding-window solution using Iterator.scanLeft and immutable data structures.
- Wiring modular components (WordSource, OutputSink, Runner) to keep concerns separated.
- Handling SIGPIPE / broken pipe cases for Unix pipelines.

## What changes were made as a result
- Implemented FunctionalTopWords.clouds using:
    - immutable.Queue for the window
    - immutable.Map for counts
    - scanLeft to generate incremental states
- Implemented FunctionalMain to read stdin, tokenize words, print clouds, flush output.
- Added broken pipe handling via IOException message check ("Broken pipe"/"EPIPE").
- Created/updated tests for correctness and interactive emission behavior.

## What I verified manually
- Ran the program via sbt and piped a text file into stdin.
- Confirmed clouds print only after the window fills, then update per new word.
- Confirmed program exits cleanly on EOF (Ctrl+D).