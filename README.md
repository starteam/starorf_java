starorf_java
============

#### Trying it online:
  Go to http://star.mit.edu/orf
  
  It is available as JNLP and executable download

#### Getting Started

1. Go to github and fork repository to your own name starteam/starorf_java
2. Use terminal to go to your eclipse workspace and run:
   ```
   git clone git@github.com:iceraj/starorf_java.git
   ```
3. Go to github and form repository to your own name starteam/star_signalsystem_java
4. Use terminal and in your eclipse worksapace run:
   ```
   git clone git@github.com:iceraj/star_signalsystem_java.git
   ```
5. Go to eclipse and run:
  - File -> Import
  - choose Existing Projects into Workspace
  - choose ‘Select root directory’ and point to the workspace
  - choose ‘star_signalsystem_java’ and ‘starorf_java’

Your workspace will build correctly and at this point you can run starorf.
Highlight starorf_java project and select ‘Debug -> as Java Application’.

#### Contributing to the project

Create pull request in git to submit patches or improvements.

#### Syncing with StarORF progress
```
git remote -v
git remote add upstream git@github.com:starteam/starorf_java.git
git git fetch upstream
git fetch upstream
git branch -va
git checkout master
git merge upstream/master
git push
```
