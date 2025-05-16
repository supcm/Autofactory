# Autofactory Thing

This project is an example of implementation of annotated classes scan to parody auto-registry thing.
Check on src/ru/supcm/autofactory/Main.java to get into.

I used @Module annotation as an example, it's a marker-annotation, but you can try it use with some other annotations.

To scan classes through packages I used a recursive algorithm ('cause it's simple and safe enough); 
if you wanna, you can speed up scan implementing faster algortihms or/and using Threads.

## Other things

Also, tou can check util package, in there I implemented Lazy Singleton and some Abstract Factory to use in the example.

Strange things of course, but why not?