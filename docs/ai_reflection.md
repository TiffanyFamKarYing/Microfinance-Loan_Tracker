## AI Reflection

### Most useful

The most useful pattern across this project was pasting real `sbt run` output back into the conversation instead of describing errors in my own words. Entry #2 is the clearest example: 11 compile errors across four files turned out to trace back to just three root causes (a missing `scalafx.Includes` import, a redundant buffer wrapper, and an ambiguous overload), and having the actual stack traces meant the fix targeted the exact lines involved rather than guessing. Entry #12 followed the same pattern later in the project, three type errors on the Borrower dropdown were all resolved from one compiler-output paste, because they shared a single underlying cause (raw JavaFX types instead of ScalaFX's wrappers).

### Where AI misled me, or I had to catch a mistake

Entry #14 is the clearest case of something going genuinely wrong. While trying to get the Borrower field's CSS to match the rest of the form, one change made both the outer ComboBox control and its inner TextField fully transparent at the same time, which removed the box's background and border entirely instead of unifying them. I only caught it because I compared the actual screenshot against what I expected and pushed back rather than assuming the fix had worked. Entry #3 is a smaller but earlier example: a suggestion to remove an "unused" `scalafx.Includes` import would have silently broken a different feature in that same file, and it was only kept after checking what the import actually enabled, not because the explanation was accepted at face value.

### What AI couldn't do

AI could generate and fix code, but it couldn't make the actual design decisions for me. Entry #9 is a good example, after several rounds of tuning a custom red button colour, deciding that the extra colour didn't actually add clarity and reverting to the plain native style was a judgement call based on how it looked in context, not something the AI could decide on my behalf. Entry #15 was similar: recognising that incremental CSS tweaking was the wrong approach, and that reusing an existing style class would be more reliable than continuing to approximate it, came from comparing screenshots myself across several failed attempts, not from the AI flagging its own approach as weak.

Overall, this project reinforced that AI is fastest when I supply it with concrete, verifiable input (real compiler output, real screenshots) rather than descriptions, and that the parts it cannot do, judging whether something actually looks or behaves right, and deciding when to change direction, stayed mine throughout.
