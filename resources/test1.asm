data SEGMENT 
var1 dw -65536
strv db 'Lorem ipsum dolor sit amet'
bvar: db 111b
dd 0fffh
data ENDS

dfnd equ [esp + ebp - 7]
thr equ 1665a12h
df equ
asf equ 17

code SEGMENT

vv db 2bh
ddw dd 17124612

start: 
		and byte ptr cs: [edx + esi + 7], eax
		neg dwVr
		jb sml
		and dfnd, ebp
		xor vv, 12

		cmp cl, strv
		xor fs: byte ptr[eax + esi + 1], 2
		jmp sml
		xor dfnd, asf
		cmp edi, [eax + ebp - 5]
		xor dwVr, 'saaa'
		cmp ecx, ddw
	
sml:    bt eax, ebx
		cmp dl, byte ptr dwVr
lbl1:  
		sar eax, 1
		stosb
		and es:strv, dh
		jb sml
		stosb
		sar bl, 1
		cmp dh, byte ptr [edi + esi + 12h]
		jmp lbl1
		neg byte ptr [eax + ebx + 2]
	
code ENdS
END
