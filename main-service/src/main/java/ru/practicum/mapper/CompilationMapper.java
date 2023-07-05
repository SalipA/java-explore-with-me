package ru.practicum.mapper;

import ru.practicum.dto.output.CompilationDto;
import ru.practicum.dto.input.NewCompilationDto;
import ru.practicum.entity.Compilation;

public class CompilationMapper {
    public static CompilationDto toCompilationDto(Compilation compilation) {
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(compilation.getId());
        compilationDto.setPinned(compilation.getPinned());
        compilationDto.setTitle(compilation.getTitle());
        return compilationDto;
    }

    public static Compilation toCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setPinned(newCompilationDto.getPinned() != null);
        return compilation;
    }
}