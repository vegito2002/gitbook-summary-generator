# Simple Gitbook Generator

## What does this do?
[Gitbook](https://www.gitbook.com/) is very useful for writers, or programmers that sometimes write. organize your markdown files like this:

<img src="https://www.dropbox.com/s/uma7ou64rmtqf28/Screenshot%202018-04-03%2023.52.49.png?raw=1" width="300">

Suppose this is a folder anywhere in your repo, and this folder's **relative** path to where the `Gen` files are located is `./demo`, then run
```
java Gen demo
```
or
```
java Gen ./demo
```




https://www.gitbook.com/book/vegito2002/gitbook-generator-demo/details

This is a simple script that can turn any directory in your repo into a base folder for your Gitbook.

Read more about folder structure from [here](https://toolchain.gitbook.com/structure.html):

Basically this is what you need to enable a folder to serve as the base of a gitbook.

```
.
├── book.json
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

For `book.json`, put it in the root of your repo, and the gitbook server will use it to find the folder you want to use as the base, an example:
```json
{
    "root": "./articles"
}
```
This will use the folder `./articles` as the base of the gitbook.

