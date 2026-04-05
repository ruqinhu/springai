package com.example.shoppingguide;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchitectureConstraintsTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.shoppingguide");
    }

    @Test
    void layer0_domain_should_not_depend_on_higher_layers() {
        // Layer 0 不能引用任何其它层（除自身及 JDK）
        noClasses().that().resideInAPackage("..domain..")
                .or().resideInAPackage("..dto..")
                .or().resideInAPackage("..exception..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..config..", "..repository..", "..agent..", "..service..", "..controller.."
                )
                .because("Harness Constraint: Layer 0 (Domain) as the core should not depend on any infrastructure or application layer.")
                .check(importedClasses);
    }

    @Test
    void layer3_controller_should_not_access_repository_layer_directly() {
        // Controller 层必须通过 Agent/Service 等中间层来操作基础设施
        noClasses().that().resideInAPackage("..controller..")
                .should().accessClassesThat().resideInAPackage("..repository..")
                .because("Harness Constraint: Layer 3 (Controller) must not bypass Layer 2 (Agent/Service) to access Layer 1 (Repository) directly.")
                .check(importedClasses);
    }

    @Test
    void layer3_controller_should_not_access_config_layer() {
        // 确保 HTTP 层不与 Config 系统强耦合
        noClasses().that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..config..")
                .because("Harness Constraint: Controllers should not be coupled with Configuration classes.")
                .check(importedClasses);
    }

}
