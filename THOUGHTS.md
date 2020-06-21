# THOUGHTS.md

A place to write down what I'm thinking about

## Paging 3

### Initial implementation

Just finished the first step of porting to paging 3: displaying data

I'm reusing a bunch of patterns from the previous iteration, such as
a repository that deals with network and disk.  I may try to port that
to RemoteMediator but I don't see too much of an advantage yet

So far Paging 3 fits my usage quite well, my Paging 2 solution had a lot
of weirdness around coroutines and such, caused by my inexperience with
both coroutines and paging.  So this rewrite of sorts should give me a
chance to make everything better, starting with the viewLifecycleScope
and ending with an easily followed data flow

Next come the "edge" cases, refreshes and errors when loading.  Also,
investigating why the docs say that `getItem` in the adapter can return
null.  Haven't run into it yet, but I also disabled placeholders because
that's a lot of UI work I want to keep separate from data layer work.

### Front page conversion success

After implementing the rest of the changes (refreshing, errors) I have
to say that Paging 3 is quite nice.  Exposing the load state separately
was already something I was doing (poorly), which can still be seen in
comments as of this commit.  Separating the different load states was
also quite handy, I was worried about the loading indicator popping up
while scrolling, but that is an APPEND and not a REFRESH, so all good.

Still not happy with all the ugliness around tags, but I'm afraid the
only solution is a good PR against lobste.rs itself

Lastly, I kind of wish SQLDelight was fully suspend, but it has so many
other advantages over Room I'm willing to deal with my extension functions