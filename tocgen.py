from jinja2 import Environment, FileSystemLoader
import os, re

env = Environment(loader=FileSystemLoader(os.path.dirname('./')))
template = env.get_template('toc.md.j2')

rootDir = '.'

list_dirs = ""
for dirName, subdirList, fileList in os.walk(rootDir):
    if re.match('^./.git',dirName):
        pass
    else:
        for fname in fileList:
            if fname == "README.md":
                list_dirs += "* [{}](#{})\n".format(dirName,os.path.join(dirName,fname).replace(" ", "%"))

vars = {"toc": list_dirs.replace("./","")}
with open('toc.md', 'w') as f:
    f.write(template.render(**vars))
