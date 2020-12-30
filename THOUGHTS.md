# THOUGHTS.md

A place to write down what I'm thinking about

## Paging 3

### Initial implementation

Just finished the first step of porting to paging 3: displaying data

I'm reusing a bunch of patterns from the previous iteration, such as a
repository that deals with network and disk.  I may try to port that to
RemoteMediator but I don't see too much of an advantage yet

So far Paging 3 fits my usage quite well, my Paging 2 solution had a lot of
weirdness around coroutines and such, caused by my inexperience with both
coroutines and paging.  So this rewrite of sorts should give me a chance to
make everything better, starting with the viewLifecycleScope and ending with an
easily followed data flow

Next come the "edge" cases, refreshes and errors when loading.  Also,
investigating why the docs say that `getItem` in the adapter can return null.
Haven't run into it yet, but I also disabled placeholders because that's a lot
of UI work I want to keep separate from data layer work.

### Front page conversion success

After implementing the rest of the changes (refreshing, errors) I have to say
that Paging 3 is quite nice.  Exposing the load state separately was already
something I was doing (poorly), which can still be seen in comments as of this
commit.  Separating the different load states was also quite handy, I was
worried about the loading indicator popping up while scrolling, but that is an
APPEND and not a REFRESH, so all good.

Still not happy with all the ugliness around tags, but I'm afraid the only
solution is a good PR against lobste.rs itself

Lastly, I kind of wish SQLDelight was fully suspend, but it has so many other
advantages over Room I'm willing to deal with my extension functions

## Comment text styling

### Finally replacing Html.fromHtml

I wanted some customization over the results of the normal Android technique to
display HTML in a text view, because certain things looked very bad, for
example, the default quote spans, and anything related to <code/>.  I put it
off for a long time because that's what I do, and also was not looking forward
to parsing and traversing HTML.  But I got fed up with the bad comment
rendering, so I added jsoup, looped over the body children, matched on tags,
recursed, and then applied a span to the concatenated result.  Rather
surprisingly, this is 90% of the work. Then all I need to do is handle all the
tags that may arise, which is a rather large possible sets of tags in theory,
but after some analysis I found out that these tags were in use across six
pages of stories:

| tag        | count |
|------------|------:|
| p          |  1753 |
| code       |   242 |
| em         |   241 |
| a          |   212 |
| blockquote |   179 |
| li         |   163 |
| ul         |    31 |
| ol         |    16 |
| strong     |    12 |
| br         |     7 |
| pre        |     4 |
| hr         |     2 |
| del        |     1 |

So, development will involve creating/finding some specific examples, and
creating a branch in the element visitor

### The last 90%

Of course the last 90% is as hard as the first 90%.  Had to think a lot about
paragraph vs character styles in order to not break things like nested lists.
Replaced some framework spans such as Quote/Bullet in order to nest correctly,
since apparently the indentation is bugged in some versions of Android.  I
think it is in a good place now, it looks good and behaves well.  More edge
cases will have to be discovered by dogfooding, however

## Local lobste.rs development

In order to make a pull request or two, and eventually to play around with
posting from the app, I decided to get started with local lobste.rs development
by following the install instructions.  As someone who is very much not into
ruby, it was a bit painful.

`rvm` installation was normal, following the lobsters readme led to an hour or
so of failing to install ruby 2.3 before I realized it's actually using 2.7,
and that went much more smoothly.  Although due to a recent big openssl upgrade
on Solus, I had to instal `openssl-11-devel` manually, while `rvm` installed
`openssl-devel`, the legacy package.  That's quite understandable though.

I already had node for some reason, so that was fine.

Running `bundle` also failed a few times, until I realized I needed the
mysql devel files.  I didn't find anything immediately in the repo, so I went
with `mariadb-devel` instead, and `bundle` worked great after that.

Wasn't sure how, or if I wanted to, have a local db, so I installed docker and
ran `mariadb` with it, which was easy.  Just had to specify `host: 127.0.0.1`
in the `config/database.yml` instead of the socket.

Trying to initialize the database is painful, even if the `rails` command
succeeds, there are a ton of warnings I'm not used to seeing, so I don't
know if they are important or not. `rails fake_data` failed on username
generation, so I hacked it to use `"username" + i.to_s` instead of the Faker
thing and it worked (spitting out tons of warnings from the cops of course).

At least after all that `rails server` worked like a charm, so that's nice.

## The Great 2020 Refresh

The big update went rather smoothly, considering how long a year is in Android Time,
no non trivial code changes had to be made, and except for the weird exception in
[buildSrc/build.gradle.kts](buildSrc/build.gradle.kts), Kotlin is nice and up to date.

To reflect on some earlier technology choices:

- Still glad I chose to go with Conductor.  Between easy transitions and straightforward
  backstack manipulation, I feel like it hits the right balance of correct defaults
  for simple situations, and power for making sure every edge case works as desired. As long
  as I continue to work with Views and anything above a few screens, I feel like I will reach
  for Conductor.  That said, I should maybe upstream something about the view lifecycle, because
  as Fragment folks have already figured out, the view lifecycle and controller lifecycle are
  different, and I don't believe conductor has that built in yet.
  
- SQLDelight/Coroutines/Retrofit makes data loading easy and straightforward: Just observe the
  database, and when refresh events happen, hop off the main thread, download, and insert.  For
  paging, the Paging library does a lot of magic that means I pretty much just need a "load a page"
  method, but I don't know how necessary that magic is. A nice future experiment would be to have a
  list of observed pages that get rendered in the adapter, then observe more pages as the user
  scrolls.
  
- Multi module navigation is still something of a question mark to me, in such a small app my
  approach of manual reflection is aggressively okay, not fun to write but very contained and
  theoretically easy to test.  I may revisit this in the future if I think of/hear about any
  better approaches.
  
Overall, I used Claw at least daily for the whole year before this refresh, with only one unknown
crash (and one known crash, due to lobste.rs changes), and look forward to another year of stability.