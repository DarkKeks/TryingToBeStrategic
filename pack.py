import os
import os.path
import zipfile

outFile = "output.zip"
replace = [['import raic.model', 'import model']]
keyWords = [['package'], ['import', 'strategy'], ['import', 'raic.RewindClient']]
sourcePath = os.path.join("src", "main", "java", "raic", "strategy")
files = []

for dirpath, dirnames, filenames in os.walk(sourcePath):
    for filename in [f for f in filenames if f.endswith(".java")]:
        files.append((filename, os.path.join(dirpath, filename)))

i = 0
with zipfile.ZipFile(outFile, mode='w', compression=zipfile.ZIP_DEFLATED) as out:
    for file in files:
        print(file[1])
        resStr = ""
        with open(file[1], "r") as inp:
            lines = inp.readlines()
            for line in lines:
                if i > 0: i -= 1
                if 'RewindClient.getInstance()' in line: i = 14

                doPrint = i == 0
                for repl in replace:
                    line = line.replace(*repl)
                for keyWord in keyWords:
                    all = True
                    for word in keyWord:
                        all &= (word in line)
                    doPrint &= not all
                if doPrint:
                    resStr += line
        out.writestr(file[0], resStr)