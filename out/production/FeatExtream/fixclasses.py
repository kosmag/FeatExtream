with open('classes.txt') as f:
	lines = f.readlines()
	for line in lines:
		print(line.replace("\"",""))