from jinja2 import Environment, FileSystemLoader
import os, glob

# Sets template to toc.md.j2 located at the root of project
env = Environment(loader=FileSystemLoader(os.path.dirname('./')))
template = env.get_template('toc.md.j2')

# Sets dir to root of project
rootDir = '.'

# Gets a list of dirs with README
list_dirs = glob.glob("./**/*.md")

content = ""

# For list of items, perform split to get the wanted parts of the path and append it to 
# content var
for items in list_dirs:
    root, dirName, readMe = items.split("/")
    link = os.path.join(dirName, readMe).replace(" ","%")
    if dirName != 'test':
        content += "[{}]({})\n".format(dirName, link)

# Saves content to object that will be used to write to template 
vars = {"toc": content}

# Writes to toc.md based on template 
with open('toc.md', 'w') as f:
    f.write(template.render(**vars))
