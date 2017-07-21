from string import Template
import fileinput
import re

"""
This script outputs a list of <dependency> entries based on the effective dependency graph
of a project.  

Usage:  the script expects the output from the dependency:tree goal.  The script looks
    for either a filename argument or (if no filename supplied) accepts input from 
    stdin.

Example:
    > mvn dependency:tree -Doutput=tree.txt 
    > python3 depgen.py tree.txt


"""



TMPL_DEPENDENCY = Template("""<dependency>
    <groupId>${groupId}</groupId>
    <artifactId>${artifactId}</artifactId>
    <version>${version}</version>
</dependency>""")


def render_dependency(**args):
    return TMPL_DEPENDENCY.substitute(args)

def process():
    dependencies = set()
    conflicts = set()
    regex = re.compile('^(.*?)\:(.*?)\:(.*?)\:(.*?)\:(.*?)')
    for line in fileinput.input():
        line = (line.strip()
            .replace('|', '')
            .replace('+-', '')
            .replace('\-', '')
            .lstrip()
            )
        is_omitted = line[0] == '('
        is_conflict = 'omitted for conflict with' in line
        if is_omitted:
            line = line[1:-1]
        if not is_omitted:
            dependencies.add(line)
        elif is_conflict:
            conflicts.add(line)
    
    for dep in sorted(dependencies):

        matches = regex.match(dep)
        if matches:
            groupId = matches.group(1)
            artifactId = matches.group(2)
            libraryType = matches.group(3)
            version = matches.group(4)
            dependencyType = matches.group(5)
            #print(dep)
            if 'org.tdar' not in groupId:
                print(render_dependency(groupId = groupId, 
                    artifactId= artifactId, 
                    version=version))




def main():
    process()

main()

#print(render_dependency(groupId = 'group', artifactId='art', version='1.6'))

