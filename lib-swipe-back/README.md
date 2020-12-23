# lib-swipe-back

Two components to enable swiping from anywhere on a screen to go back 
(or trigger any action really)

Pairs nicely with a RouterTransition that also manipulates the translationX

Unfortunately I wrote it a while ago and didn't take great notes, so I'll reconstruct
what I found out by extracting this lib

## Usage

- Make the `TouchInterceptingCoordinatorLayout` the root
- assign a `SwipeBackListener` to the `root.listener`
- If any area of the root is *not* covered by something that accepts touches,
  make sure to set the root as clickable/focusable (not sure if the a11y semantics
  are correct though, sorry), otherwise there will be no touches to intercept
  
  It appears that normal `AppBarLayout` and `SwipeRefreshLayout` combo covers everything
  needed however