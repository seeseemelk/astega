METHOD=parity

encode:
	java -jar astega.jar ${METHOD} encode data.bin cirice.wav cirice_coded.wav

decode:
	java -jar astega.jar ${METHOD} decode cirice_coded.wav decoded.bin

test: encode decode
	sha256sum data.bin decoded.bin

dump: encode decode
	hexdump data.bin -C | head
	hexdump decoded.bin -C | head
