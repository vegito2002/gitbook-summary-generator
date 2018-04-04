# Lightweight Gitbook Generator

This is a simple script that can turn any directory in your repo into a base folder for your Gitbook.

## What Does This Do?
[Gitbook](https://www.gitbook.com/) is very useful for writers, or programmers that sometimes write. 

### Getting it to run now

Organize your markdown files like this:

<img src="https://www.dropbox.com/s/uma7ou64rmtqf28/Screenshot%202018-04-03%2023.52.49.png?raw=1" width="300">

Suppose this is a book base folder anywhere in your repo, and this folder's **relative** path to where the `Gen` files are located is `./demo`, then run
```
$ java Gen demo
```
or
```
$ java Gen ./demo
```

And add a `README.md` and a `book.json`:
```json
{
    "root": "./demo"
}
```
to the **root of your repo**. The folder `./demo` is ready to be linked to your gitbook. Just create a book in Gitbook and link to your repo. 

### List Of Features
This is a list of features introduced in detail below
* Recursively generating `SUMMARY.md`.
* File name splitting, which allows user-specified regex rules.
* Arbitrary target position for the book base folder.
* Full confirmation to official Gitbook documentation specification.
* Additional file content processing:
    * Newline handling to avoid github's append-two-spaces-at-end rule
    * Automatically handling of dropbox direct screenshot link. Additional width-attribute scaling enabled.

## A Little More Explanation
This is a compact script. All you need is Java for it to work. Copy `Gen.class` (or `Gen.java` if you want to compile yourself) to a convenient folder, preferably the root of your repo.

The introduction above aims to get you going as quick as possible. Here are some more explanation. First, please refer to the wonderful documentation [here](https://toolchain.gitbook.com/structure.html) for detailed information. Here I give as little information as possible.

First, I provided a sample source folder `./original_demo` that you can experiment with. Copy everything in that folder to `./demo` and run the script as shown in the previous section to see the result:
* `SUMMARY.md` is generated.
* Additional file content processing is provided, as discussed below. To enable this processing run `$ java Gen demo true` instead.

The book generated is provided [here](https://www.gitbook.com/book/vegito2002/gitbook-generator-demo/details). There will be short gitbook linking instructions in the end of this article.

Organize your folder like this (placed anywhere in your repo, as long as you know the relative path to the root, which we denote as `base_to_root`):
```
.
├── README.md
├── SUMMARY.md
├── chapter-1/
|   ├── README.md
|   └── something.md
└── chapter-2/
    ├── README.md
    └── something.md
```

**File** | **Description** | **Notes**
----|----|----
`book.json` | Stores configuration data (**optional**) | See below.
`README.md` |  Preface / Introduction for your book (**required**) | **Attention**: This is the cover of your book, and you have to supply this. 
`SUMMARY.md` |  Table of Contents (See Pages) (**optional**) | You don't have to worry about this. If you already have this file, it will be overwritten, so back it up if necessary.

For `book.json`, put it in the **root of your repo**, and the gitbook server will use it to find the folder you want to use as the base, an example:
```json
{
    "root": "./articles"
}
```
This will use the folder `./articles` as the base of the gitbook. I could have automated this part, but this reduced the flexibility of the script. It would be useful to seperate the *root of the repo*, *base folder that store the book files* and *folder that has `Gen` script*. 

### Suggestions on File Organization
These are not hard guidelines: as long as you confirm to the required structure shown above, which is what Gitbook itself asks, you are okay to go. But generally, in a structure like:
```
.
├── README.md
├── SUMMARY.md
├── chapter-1/
|   ├── README.md
|   └── something.md
└── chapter-2/
    ├── README.md
    └── something.md
```
The `README.md` in the root of your book base folder has been introduced above: it's the introduction cover of your book.

You usually want to use each folder as a part/chapter/subsection further down. In the folder `chapter-1`, you can optionally include a `README.md` file, which will act as the chapter cover for this folder's chapter. Note that this `README.md` is different from the `README.md` in the root of the book base folder.

It is not advised to include space in any of the file names. I personally advise using `TextOne.md` if you want it to be shown as an article named **Text One** in the book. The script by default will take care of that for you.

### Everything not mentioned yet
The basic functionality this script provides is **generating `SUMMARY.md`** and **split file names**. There are something additional you can do.

When you supply command line arguments, do make sure to stick to something like
```
$ java Gen demo true
```
In the specified order, where `demo` specifies the `base_to_root` relative path, and `true` specifies that you want the additional file content processing mentioned below to be carried out. Both are optional, but you have to specify `demo` (default value is `.` if you are already in the book base folder) before you can supply `true`.

Now, about file content processing, these are purely personal preferences. I don't like how newline is handled in github markdown (newline alone does not give you a new line, you have to append two spaces to the end of the line), so I delt with that. Also, I constantly insert dropbox screenshot links like this:
```
https://www.dropbox.com/s/gtk56vs39qvjtzi/Screenshot%202018-04-04%2000.32.28.png?dl=0
```
in my markdown files. Instead of writing `![]()` or `<img src="" width="500">` yourself, just leave the line like that, and it will work. 

Such linkes are automatically put in your clipboard when you have dropbox on your macbook installed and this configuration enabled:

<img src="https://www.dropbox.com/s/7njhvb7udnt9g0m/Screenshot%202018-04-04%2000.34.37.png?raw=1" width="500">

Please Google the corresponding configuration if you are using Window.

Additionally, you can optionally append a whitespace-separated width-scaling attribute after then url. An example in a nutshell:
```
https://www.dropbox.com/s/gtk56vs39qvjtzi/Screenshot%202018-04-04%2000.32.28.png?dl=0 800
```
will be converted to
```
<img src="https://www.dropbox.com/s/gtk56vs39qvjtzi/Screenshot%202018-04-04%2000.32.28.png?raw=1" width="800">
```

One final thing, about how the file names is split. You can supply a `regex.md` at the *folder of `Gen` script* so that you can specify the regex rules you want applied to name splitting. You will supply `pattern` and `seperate` pair in the file as alternating lines, which will be used like:
```java
file_name.replaceAll (pattern, seperator);
```
I have to say this is a limitted feature. You have to store something like
```
pattern1
seperator1
pattern2
seperator2
...
```
in the `regex.md` file in this line-alternating style. Each of the rule pair will be applied sequentially, so you have to organize your logic if you have more than one rules.  
If this file is not supplied, the default rule is applied, which is virtually equivalant to having this `regex.md` file:
```
(?<=[^A-Z&&\S])(?=[A-Z])
 
```
The second line only has a space. This rule actually do the `TextOne -> Text One` thing mentioned above. 

## Setting up Your Gitbook
This is not relevant to this script, but just put here to save you a little Googling.

To have a Gitbook, there are various ways to go, please refer to the [full documentation](https://toolchain.gitbook.com/structure.html). My personal favorite is to sync a book with a repo, and afterwards each of your github commit will now trigger an update to the book automatically.

To do that, first use my script to set up the book base folder as discussed above. Or manually according to the documentation if you prefer. Do remember that your github repo should have the properly set `book.json` at the root of the repo, so gitbook server knows how to reach the book base folder from your repo root.

I could elaborate, but [**this page**](https://help.gitbook.com/github/can-i-host-on-github.html#github-integration) really says it all.
