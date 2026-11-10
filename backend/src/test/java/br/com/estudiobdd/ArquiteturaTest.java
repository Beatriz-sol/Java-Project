package br.com.estudiobdd;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Regras de arquitetura verificadas a cada mvn test. */
class ArquiteturaTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importar() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.com.estudiobdd");
    }

    @Test
    void controllersNaoAcessamRepositorios() {
        noClasses().that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .because("o controller deve passar pelo serviço (arquitetura em cinco camadas)")
                .check(classes);
    }

    @Test
    void dominioNaoDependeDasCamadasDeCima() {
        noClasses().that().resideInAPackage("..model..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..controller..", "..service..", "..repository..", "..dto..")
                .check(classes);
    }
}
