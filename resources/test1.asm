data SEGMENT
var1 dw 22aeh
strv db 'Lorem ipsum dolor sit amet'
bvar db 10010101b
dwVr dd 0a184dh
data ENDS

dfnd equ [esp + esi - 7]
thr equ 1a65e7h

code SEGMENT
vv db 2h
ddw dd 17124612

start:
		and [edx + edi - 7], eax
		neg dwVr
		jb sml
		and dfnd, ebp
		xor vv, 10h
lbl:
		cmp cl, strv
		xor [ebp + ecx + 2], 1a3e5fh
		jmp sml
		xor ss:dfnd, 0a141fh
		cmp edi, [eax + ebp + 2]
		xor gs:dwVr, 11b
		cmp ecx, ddw

sml:    bt eax, ebx
		cmp dl, byte ptr dwVr
		db '0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000'
lbl1:
		sar ebp, 1
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