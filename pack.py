import os.path
import zipfile

outFile = "output.zip"
replace = [['import raic.model', 'import model']]
keyWords = [['package'], ['import', 'strategy'], ['import', 'raic.RewindClient']]
skipAfter = [['RewindClient.getInstance()', 14]]
sourcePath = os.path.join("src", "main", "java", "raic", "strategy")
files = []

for dirpath, dirnames, filenames in os.walk(sourcePath):
    for filename in [f for f in filenames if f.endswith(".java")]:
        files.append((filename, os.path.join(dirpath, filename)))

skip = 0
with zipfile.ZipFile(outFile, mode='w', compression=zipfile.ZIP_DEFLATED) as out:
    for file in files:
        print(file[1])
        resStr = ""
        with open(file[1], "r") as inp:
            lines = inp.readlines()
            for line in lines:
                if skip > 0: skip -= 1

                if 'RewindClient.getInstance()' in line: i = 14
                for rec in skipAfter:
                    if rec[0] in line: skip = rec[1]

                doPrint = skip == 0
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