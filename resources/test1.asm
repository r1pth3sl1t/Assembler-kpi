data SEGMENT 
var1 dw -116
strv db 'Lorem ipsum dolor sit amet'
bvar db 1001010b
dd -0a184dh
data ENDS

dfnd equ [esp + ebp - 7]
thr equ 1a65e7h
df equ
asf equ (18 + 5 + 7 * (5 - 1 + 16 * (17 - 2) + 3) - 10 / 5) - 10

code SEGMENT 

vv db 2h
ddw dd 17124612

start: 
		and byte ptr cs: [edx + esi + 7], eax
		neg dwVr
		jb sml
		and dfnd, ebp
		xor vv, asf
lbl:
		cmp cl, strv
		xor [ebp + ecx + 2], 1 + 7 - (5 + 2 * (7 + 1 - 5) - 30) + 12
		jmp sml
		xor dfnd, 0a141fh
		cmp edi, [eax + ebp - 5]
		xor dwVr, 11b
		cmp ecx, ddw
	
sml:    bt eax, ebx
		cmp dl, byte ptr dwVr
lbl1:  
		sar eax, 1
		stosb
		and fs:strv, dh
		jb sml
		stosb
		sar bl, 1
		cmp dh, byte ptr [edi + esi + 12h]
		jmp lbl1
		neg byte ptr [eax + ebx + 2]
	
code ENDS
END
