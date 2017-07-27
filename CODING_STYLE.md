# Java language rules

### Don't ignore exceptions

It can be tempting to write code that completely ignores an exception, such as:

```java
  void setServerPort(String value) {
      try {
          serverPort = Integer.parseInt(value);
      } catch (NumberFormatException e) { }
  }
```

Do not do this. While you may think your code will never encounter this error condition or that it is not important to handle it, ignoring exceptions as above creates mines in your code for someone else to trigger some day. You must handle every Exception in your code in a principled way; the specific handling varies depending on the case.

_Anytime somebody has an empty catch clause they should have a creepy feeling. There are definitely times when it is actually the correct thing to do, but at least you have to think about it. In Java you can't escape the creepy feeling._ [-James Gosling](http://www.artima.com/intv/solid4.html)

Acceptable alternatives (in order of preference) are:

* Throw the exception up to the caller of your method.
```java
  void setServerPort(String value) throws NumberFormatException {
      serverPort = Integer.parseInt(value);
  }
```
* Throw a new exception that's appropriate to your level of abstraction.
```java
  void setServerPort(String value) throws ConfigurationException {
      try {
          serverPort = Integer.parseInt(value);
      } catch (NumberFormatException e) {
          throw new ConfigurationException("Port " + value + " is not valid.");
      }
  }
```
* Handle the error gracefully and substitute an appropriate value in the catch {} block.
```java
  /** Set port. If value is not a valid number, 80 is substituted. */
  
  void setServerPort(String value) {
      try {
          serverPort = Integer.parseInt(value);
      } catch (NumberFormatException e) {
          serverPort = 80;  // default port for server
      }
  }
```
* Catch the Exception and throw a new RuntimeException. This is dangerous, so do it only if you are positive that if this error occurs the appropriate thing to do is crash.
```java
  /** Set port. If value is not a valid number, die. */
  
  void setServerPort(String value) {
      try {
          serverPort = Integer.parseInt(value);
      } catch (NumberFormatException e) {
          throw new RuntimeException("port " + value " is invalid, ", e);
      }
  }
```
* As a last resort, if you are confident that ignoring the exception is appropriate then you may ignore it, but you must also comment why with a good reason:
```java
  /** If value is not a valid number, original port number is used. */
  void setServerPort(String value) {
      try {
          serverPort = Integer.parseInt(value);
      } catch (NumberFormatException e) {
          // Method is documented to just ignore invalid user input.
          // serverPort will just be unchanged.
      }
  }
```

### Don't catch generic exception

Try to avoid doing this:

```java
try {
    someComplicatedIOFunction();        // may throw IOException
    someComplicatedParsingFunction();   // may throw ParsingException
    someComplicatedSecurityFunction();  // may throw SecurityException
    // phew, made it all the way
} catch (Exception e) {                 // I'll just catch all exceptions
    handleError();                      // with one generic handler!
}
```
Multiple Exceptions can mean multiple errors, it can be a network error, a parsing, etc, so ideally we should act accordingly.

See more reasons why and some alternatives [here](https://source.android.com/source/code-style.html#dont-catch-generic-exception)

### Fully qualify imports

This is bad: `import foo.*;`

This is good: `import foo.Bar;`

See more info [here](https://source.android.com/source/code-style.html#fully-qualify-imports)

# Java style rules

## Rules common to all identifiers
Identifiers use only ASCII letters and digits, and, in a small number of cases noted below, underscores. Thus each valid identifier name is matched by the regular expression `\w+` .

Special prefixes or suffixes, like those seen in the examples `name_`, `mName`, `s_name` and `kName`, are **not** used.

## Rules by identifier

### Package names
Package names are all lowercase, with consecutive words simply concatenated together (no underscores). For example, `com.example.deepspace`, **not** `com.example.deepSpace` or `com.example.deep_space`.

### Class names
Class names are written in UpperCamelCase.

Class names are typically nouns or noun phrases. For example, `Character` or `ImmutableList`. Interface names may also be nouns or noun phrases (for example, `List`), but may sometimes be adjectives or adjective phrases instead (for example, `Readable`).

There are no specific rules or even well-established conventions for naming annotation types.

Test classes are named starting with the name of the class they are testing, and ending with `Test`. For example, `HashTest` or `HashIntegrationTest`.

### Method names
Method names are written in lowerCamelCase.

Method names are typically verbs or verb phrases. For example, `sendMessage` or `stop`.

Underscores may appear in JUnit test method names to separate logical components of the name. One typical pattern is `test<MethodUnderTest>_<state>`, for example `testPop_emptyStack`. There is no One Correct Way to name test methods.

### Constant names
Constant names use CONSTANT_CASE: all uppercase letters, with words separated by underscores. But what is a constant, exactly?

Constants are static final fields whose contents are deeply immutable and whose methods have no detectable side effects. This includes primitives, Strings, immutable types, and immutable collections of immutable types. If any of the instance's observable state can change, it is not a constant. Merely intending to never mutate the object is not enough. Examples:
```java
// Constants
static final int NUMBER = 5;
static final ImmutableList<String> NAMES = ImmutableList.of("Ed", "Ann");
static final ImmutableMap<String, Integer> AGES = ImmutableMap.of("Ed", 35, "Ann", 32);
static final Joiner COMMA_JOINER = Joiner.on(','); // because Joiner is immutable
static final SomeMutableType[] EMPTY_ARRAY = {};
enum SomeEnum { ENUM_CONSTANT }

// Not constants
static String nonFinal = "non-final";
final String nonStatic = "non-static";
static final Set<String> mutableCollection = new HashSet<String>();
static final ImmutableSet<SomeMutableType> mutableElements = ImmutableSet.of(mutable);
static final ImmutableMap<String, SomeMutableType> mutableValues =
    ImmutableMap.of("Ed", mutableInstance, "Ann", mutableInstance2);
static final Logger logger = Logger.getLogger(MyClass.getName());
static final String[] nonEmptyArray = {"these", "can", "change"};
```
These names are typically nouns or noun phrases.

### Non-constant field names
Non-constant field names (static or otherwise) are written in lowerCamelCase.             

These names are typically nouns or noun phrases. For example, `computedValues` or `index`.

The greater the scope, more indicative must be the variable name. For example a variable with a loop scope could be called just `i`, while on function scope it could be `index`, and a class member variable would be `currentIndex`.

### Parameter names
Parameter names are written in lowerCamelCase.

One-character parameter names in public methods should be avoided.

### Local variable names
Local variable names are written in lowerCamelCase.

Even when final and immutable, local variables are not considered to be constants, and should not be styled as constants.
             
### Type variable names
Each type variable is named in one of two styles:
             
A single capital letter, optionally followed by a single numeral (such as `E`, `T`, `X`, `T2`)
A name in the form used for classes (see Section 5.2.2, Class names), followed by the capital letter T (examples: `RequestT`, `FooBarT`).

## Limit variable scope

_The scope of local variables should be kept to a minimum (Effective Java Item 29). By doing so, you increase the readability and maintainability of your code and reduce the likelihood of error. Each variable should be declared in the innermost block that encloses all uses of the variable._

_Local variables should be declared at the point they are first used. Nearly every local variable declaration should contain an initializer. If you don't yet have enough information to initialize a variable sensibly, you should postpone the declaration until you do._ - ([Android code style guidelines](https://source.android.com/source/code-style.html#limit-variable-scope))

## Order import statements

If you are using an IDE such as Android Studio, you don't have to worry about this because your IDE is already obeying these rules. If not, have a look below.

The ordering of import statements is:

1. Android imports
2. Imports from third parties (com, junit, net, org)
3. java and javax
4. Same project imports

To exactly match the IDE settings, the imports should be:

* Alphabetically ordered within each grouping, with capital letters before lower case letters (e.g. Z before a).
* There should be a blank line between each major grouping (android, com, junit, net, org, java, javax).

More info [here](https://source.android.com/source/code-style.html#limit-variable-scope)

## Class member ordering

There is no single correct solution for this but using a __logical__ and __consistent__ order will improve code learnability and readability. It is recommendable to use the following order:

1. Constants
2. Fields
3. Constructors
4. Override methods and callbacks (public or private)
5. Public methods
6. Private methods
7. Inner classes or interfaces

Example:

```java
public class MainActivity extends Activity {

	private String title;
    private TextView textViewTitle;

    public void setTitle(String title) {
    	this.title = title;
    }

    @Override
    public void onCreate() {
        ...
    }

    private void setUpView() {
        ...
    }

    static class AnInnerClass {

    }

}
```

If your class is extending an __Android component__ such as an Activity or a Fragment, it is a good practice to order the override methods so that they __match the component's lifecycle__. For example, if you have an Activity that implements `onCreate()`, `onDestroy()`, `onPause()` and `onResume()`, then the correct order is:

```java
public class MainActivity extends Activity {

	//Order matches Activity lifecycle
    @Override
    public void onCreate() {}

    @Override
    public void onResume() {}

    @Override
    public void onPause() {}

    @Override
    public void onDestroy() {}

}
```

## Parameter ordering in methods

When programming for Android, it is quite common to define methods that take a `Context`. If you are writing a method like this, then the __Context__ must be the __first__ parameter.

The opposite case are __callback__ interfaces that should always be the __last__ parameter.

Examples:

```java
// Context always goes first
public User loadUser(Context context, int userId);

// Callbacks always go last
public void loadUserAsync(Context context, int userId, UserCallback callback);
```

##  String constants, naming, and values

Many elements of the Android SDK such as `SharedPreferences`, `Bundle`, or `Intent` use a key-value pair approach so it's very likely that even for a small app you end up having to write a lot of String constants.

When using one of these components, you __must__ define the keys as a `static final` fields and they should be prefixed as indicated below.

| Element            | Field Name Prefix |
| -----------------  | ----------------- |
| SharedPreferences  | `PREF_`             |
| Bundle             | `BUNDLE_`           |
| Fragment Arguments | `ARGUMENT_`         |
| Intent Extra       | `EXTRA_`            |
| Intent Action      | `ACTION_`           |

Note that the arguments of a Fragment - `Fragment.getArguments()` - are also a Bundle. However, because this is a quite common use of Bundles, we define a different prefix for them.

Example:

```java
// Note the value of the field is the same as the name to avoid duplication issues
static final String PREF_EMAIL = "PREF_EMAIL";
static final String BUNDLE_AGE = "BUNDLE_AGE";
static final String ARGUMENT_USER_ID = "ARGUMENT_USER_ID";

// Intent-related items use full package name as value
static final String EXTRA_SURNAME = "com.myapp.extras.EXTRA_SURNAME";
static final String ACTION_OPEN_USER = "com.myapp.action.ACTION_OPEN_USER";
```

# Camel case: defined
Sometimes there is more than one reasonable way to convert an English phrase into camel case, such as when acronyms or unusual constructs like "IPv6" or "iOS" are present. To improve predictability, Conding Style specifies the following (nearly) deterministic scheme.

Beginning with the prose form of the name:

1. Convert the phrase to plain ASCII and remove any apostrophes. For example, "Müller's algorithm" might become "Muellers algorithm".
2. Divide this result into words, splitting on spaces and any remaining punctuation (typically hyphens).

          Recommended: if any word already has a conventional camel-case appearance in common usage, split this into its constituent parts (e.g., "AdWords" becomes "ad words"). Note that a word such as "iOS" is not really in camel case per se; it defies any convention, so this recommendation does not apply.
3. Now lowercase everything (including acronyms), then uppercase only the first character of:

          ... each word, to yield upper camel case, or
          ... each word except the first, to yield lower camel case
4. Finally, join all the words into a single identifier.
Note that the casing of the original words is almost entirely disregarded. Examples:

| Prose form              | Correct           | Incorrect         |
| ----------------------- | ----------------- | ----------------- |
| "XML HTTP request"      | `XmlHttpRequest`    | `XMLHTTPRequest`    |
| "new customer ID"	      | `newCustomerId`     | `newCustomerID`      |
| "inner stopwatch"	      | `innerStopwatch`    | `innerStopWatch`     |
| "supports IPv6 on iOS?" | `supportsIpv6OnIos` | `supportsIPv6OnIOS`  |
| "YouTube importer"	  | `YouTubeImporter` `YoutubeImporter`*	  |
*Acceptable, but not recommended.

          Note: Some words are ambiguously hyphenated in the English language: for example "nonempty" and "non-empty" are both correct, so the method names checkNonempty and checkNonEmpty are likewise both correct.

# Indentation
### Use spaces for indentation

Use __4 space__ indents for blocks:

```java
if (x == 1) {
    x++;
}
```

Use __8 space__ indents for line wraps:

```java
Instrument i =
        someLongExpression(that, wouldNotFit, on, one, line);
```

### Use standard brace style

Braces go on the same line as the code before them.

```java
class MyClass {
    int func() {
        if (something) {
            // ...
        } else if (somethingElse) {
            // ...
        } else {
            // ...
        }
    }
}
```

Braces around the statements are **required** unless the condition and the body fit on one line.

If the condition and the body fit on one line and that line is shorter than the max line length, then braces are not required, e.g.

```java
if (condition) body();
```

This is __bad__:

```java
if (condition)
    body();  // bad!
```

### Line length limit

Code lines should not exceed __100 characters__. If the line is longer than this limit there are usually two options to reduce its length:

* Extract a local variable or method (preferable).
* Apply line-wrapping to divide a single line into multiple ones.

There are two __exceptions__ where it is possible to have lines longer than 100:

* Lines that are not possible to split, e.g. long URLs in comments.
* `package` and `import` statements.

#### Line-wrapping strategies

There isn't an exact formula that explains how to line-wrap and quite often different solutions are valid. However there are a few rules that can be applied to common cases.

__Break at operators__

When the line is broken at an operator, the break comes __before__ the operator. For example:

```java
int longName = anotherVeryLongVariable + anEvenLongerOne - thisRidiculousLongOne
        + theFinalOne;
```

__Assignment Operator Exception__

An exception to the `break at operators` rule is the assignment operator `=`, where the line break should happen __after__ the operator.

```java
int longName =
        anotherVeryLongVariable + anEvenLongerOne - thisRidiculousLongOne + theFinalOne;
```

__Method chain case__

When multiple methods are chained in the same line - for example when using Builders - every call to a method should go in its own line, breaking the line before the `.`

```java
Picasso.with(context).load("http://ribot.co.uk/images/sexyjoe.jpg").into(imageView);
```

```java
Picasso.with(context)
        .load("http://ribot.co.uk/images/sexyjoe.jpg")
        .into(imageView);
```

__Long parameters case__

When a method has many parameters or its parameters are very long, we should break the line after every comma `,`

```java
loadPicture(context, "http://ribot.co.uk/images/sexyjoe.jpg", mImageViewProfilePicture, clickListener, "Title of the picture");
```

```java
loadPicture(context,
        "http://ribot.co.uk/images/sexyjoe.jpg",
        mImageViewProfilePicture,
        clickListener,
        "Title of the picture");
```

### RxJava chains styling

Rx chains of operators require line-wrapping. Every operator must go in a new line and the line should be broken before the `.`

```java
public Observable<Location> syncLocations() {
    return databaseHelper.getAllLocations()
            .concatMap(new Func1<Location, Observable<? extends Location>>() {
                @Override
                 public Observable<? extends Location> call(Location location) {
                     return mRetrofitService.getLocation(location.id);
                 }
            })
            .retry(new Func2<Integer, Throwable, Boolean>() {
                 @Override
                 public Boolean call(Integer numRetries, Throwable throwable) {
                     return throwable instanceof RetrofitError;
                 }
            });
}
```


# Annotations

### Annotations practices

According to the [Android code style guide](https://source.android.com/source/code-style), the standard practices for some of the predefined annotations in Java are:

* `@Override`: The @Override annotation __must be used__ whenever a method overrides the declaration or implementation from a super-class. For example, if you use the @inheritdocs Javadoc tag, and derive from a class (not an interface), you must also annotate that the method @Overrides the parent class's method.

* `@SuppressWarnings`: The @SuppressWarnings annotation should only be used under circumstances where it is impossible to eliminate a warning. If a warning passes this "impossible to eliminate" test, the @SuppressWarnings annotation must be used, so as to ensure that all warnings reflect actual problems in the code.

More information about annotation guidelines can be found [here](http://source.android.com/source/code-style.html#use-standard-java-annotations).

### Annotations style

__Classes, Methods and Constructors__

When annotations are applied to a class, method, or constructor, they are listed after the documentation block and should appear as __one annotation per line__ .

```java
/* This is the documentation block about the class */
@AnnotationA
@AnnotationB
public class MyAnnotatedClass { }
```

__Fields__

Annotations applying to fields should be listed __on one line__, unless the line reaches the maximum line length, and the field declaration on the line bellow.

```java
@Nullable @Mock
DataManager dataManager;
```

#XML style rules

### Use self closing tags

When an XML element doesn't have any contents, you __must__ use self closing tags.

This is good:

```xml
<TextView
	android:id="@+id/text_view_profile"
	android:layout_width="wrap_content"
	android:layout_height="wrap_content" />
```

This is __bad__ :

```xml
<!-- Don\'t do this! -->
<TextView
    android:id="@+id/text_view_profile"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" >
</TextView>
```


### Resources naming

Resource IDs and names are written in __lowercase_underscore__.

#### ID naming

IDs should be prefixed with the name of the element in lowercase underscore. For example:


| Element            | Prefix            |
| -----------------  | ----------------- |
| `TextView`           | `text_`             |
| `ImageView`          | `image_`            |
| `Button`             | `button_`           |
| `Menu`               | `menu_`             |

Image view example:

```xml
<ImageView
    android:id="@+id/image_profile"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

Menu example:

```xml
<menu>
	<item
        android:id="@+id/menu_done"
        android:title="Done" />
</menu>
```

#### Strings

String names start with a prefix that identifies the section they belong to. For example `registration_email_hint` or `registration_name_hint`. If a string __doesn't belong__ to any section, then you should follow the rules below:


| Prefix             | Description                           |
| -----------------  | --------------------------------------|
| `error_`             | An error message                      |
| `msg_`               | A regular information message         |
| `title_`             | A title, i.e. a dialog title          |
| `action_`            | An action such as "Save" or "Create"  |


#### Styles and Themes

Unless the rest of resources, style names are written in __UpperCamelCase__.

### Attributes ordering

As a general rule you should try to group similar attributes together. A good way of ordering the most common attributes is:

1. View Id
2. Style
3. Layout width and layout height
4. Other layout attributes, sorted alphabetically
5. Remaining attributes, sorted alphabetically

# Kotlin
## Naming Style
If in doubt, default to the Java Coding Conventions such as:

- use of camelCase for names (and avoid underscore in names)
- types start with upper case
- methods and properties start with lower case
- use 4 space indentation
- public functions should have documentation such that it appears in Kotlin Doc

## Colon
There is a space before colon where colon separates type and supertype and there's no space where colon separates instance and type:
```kotlin
interface Foo<out T : Any> : Bar {
    fun foo(a: Int): T
}
```

## Lambdas

In lambda expressions, spaces should be used around the curly braces, as well as around the arrow which separates the parameters from the body. Whenever possible, a lambda should be passed outside of parentheses.

```kotlin
list.filter { it > 10 }.map { element -> element * 2 }
```
In lambdas which are short and not nested, it's recommended to use the it convention instead of declaring the parameter explicitly. In nested lambdas with parameters, parameters should be always declared explicitly.

## Class header formatting
Classes with a few arguments can be written in a single line:

```kotlin
class Person(id: Int, name: String)
```
Classes with longer headers should be formatted so that each primary constructor argument is in a separate line with indentation. Also, the closing parenthesis should be on a new line. If we use inheritance, then the superclass constructor call or list of implemented interfaces should be located on the same line as the parenthesis:

```kotlin
class Person(
    id: Int, 
    name: String,
    surname: String
) : Human(id, name) {
    // ...
}
```
For multiple interfaces, the superclass constructor call should be located first and then each interface should be located in a different line:

```kotlin
class Person(
    id: Int, 
    name: String,
    surname: String
) : Human(id, name),
    KotlinMaker {
    // ...
}
```
Constructor parameters can use either the regular indent or the continuation indent (double the regular indent).

## Unit
If a function returns Unit, the return type should be omitted:

```kotlin
fun foo() { // ": Unit" is omitted here

}
```

## Functions vs Properties
In some cases functions with no arguments might be interchangeable with read-only properties. Although the semantics are similar, there are some stylistic conventions on when to prefer one to another.

Prefer a property over a function when the underlying algorithm:

- does not throw
- has a O(1) complexity
- is cheap to calculate (or caсhed on the first run)
- returns the same result over invocations

# Tests style rules

## Unit tests

Test classes should match the name of the class the tests are targeting, followed by `Test`. For example, if we create a test class that contains tests for the `DatabaseHelper`, we should name it `DatabaseHelperTest`.

Test methods are annotated with `@Test` and should generally start with the name of the method that is being tested, followed by a precondition and/or expected behaviour.

* Template: `@Test void methodNamePreconditionExpectedBehaviour()`
* Example: `@Test void signInWithEmptyEmailFails()`

Precondition and/or expected behaviour may not always be required if the test is clear enough without them.

Sometimes a class may contain a large amount of methods, that at the same time require several tests for each method. In this case, it's recommendable to split up the test class into multiple ones. For example, if the `DataManager` contains a lot of methods we may want to divide it into `DataManagerSignInTest`, `DataManagerLoadUsersTest`, etc. Generally you will be able to see what tests belong together because they have common [test fixtures](https://en.wikipedia.org/wiki/Test_fixture).

### 1.4.2 Espresso tests

Every Espresso test class usually targets an Activity, therefore the name should match the name of the targeted Activity followed by `Test`, e.g. `SignInActivityTest`

When using the Espresso API it is a common practice to place chained methods in new lines.

```java
onView(withId(R.id.view))
        .perform(scrollTo())
        .check(matches(isDisplayed()))
```

# License

```
Copyright 2017 Rocket Chat

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```