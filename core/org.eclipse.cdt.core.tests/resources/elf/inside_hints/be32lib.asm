; ELF Header
db 0x7F, 'E', 'L', 'F'; Magic start
db 0x01; EI_CLASS: 1: 32 bit, 2: 64 bit
db 0x02; EI_DATA: 1: LE, 2: BE
db 0x01; EI_VERSION: 1: Original ELF
db 0x00; EI_OSABI: 00: System-V, 03: Linux, mostly 0x00 regardless
db 0x00; EI_ABIVERSION: mostly unused therefore 0x00
db 0x00; EI_PAD
dw 0x0000; EI_PAD
dd 0x00000000; EI_PAD
dw 0x0300; e_type: ET_DYN
dw 0x0300; e_machine x86
dd 0x01000000; e_version
dd 0x58000000; e_entry
dd 0x34000000; e_phoff
dd 0x00000000; e_shoff
dd 0x00000000; e_flags
dw 0x3400; e_ehsize
dw 0x2000; e_phentsize
dw 0x0100; e_phnum
dw 0x2800; e_shentsize
dw 0x0000; e_shnum
dw 0x0000; e_shstrndx

; Program Header 1
dd 0x00000000; p_type: PT_NULL
dd 0x00000000; p_offset
dd 0x00000000; p_vaddr
dd 0x00000000; p_paddr
dd 0x00000000; p_filesz
dd 0x00000000; p_memsz
dd 0x00000000; p_flags, 32 bit
dd 0x00000000; p_align

dd 0x03000000; random value