/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openwms.wms.movements;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.ameba.annotation.TxService;
import org.slf4j.Logger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * A EnsureArchitectureIT.
 *
 * @author Heiko Scherrer
 */
@AnalyzeClasses(packages = "org.openwms.tms", importOptions = {ImportOption.DoNotIncludeTests.class})
class EnsureArchitectureIT {

    @ArchTest
    public static final ArchRule verify_logger_definition =
        fields().that().haveRawType(Logger.class)
                .should().bePrivate()
                .andShould().beStatic()
                .andShould().beFinal()
                .because("This a an agreed convention")
            ;

    @ArchTest
    public static final ArchRule verify_api_package =
            classes().that()
                    .resideInAPackage("..tms.api..")
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage("..tms.api..", "org.openwms.core..", "org.openwms.common..", "java..", "org.springframework..")
                    .because("The API package is separated and the only package accessible by the client")
            ;

    @ArchTest
    public static final ArchRule verify_services =
            classes().that()
                    .areAnnotatedWith(TxService.class)
                    .or()
                    .areAnnotatedWith(Service.class)
                    .should()
                    .bePackagePrivate()
                    .andShould()
                    .resideInAnyPackage("..impl..", "..commands..", "..events..")
                    .because("By convention Transactional Services should only reside in internal packages")
            ;

    @ArchTest
    public static final ArchRule verify_transactional_repository_access =
            classes().that()
                    .areAnnotatedWith(Repository.class)
                    .or()
                    .areAssignableFrom(JpaRepository.class)
                    .should()
                    .bePackagePrivate()
                    .andShould()
                    .onlyHaveDependentClassesThat()
                    .areAnnotatedWith(TxService.class)
                    .orShould()
                    .onlyHaveDependentClassesThat()
                    .areAnnotatedWith(Transactional.class)
                    .because("A Repository must only be access in a transaction context")
            ;

    @ArchTest
    public static final ArchRule verify_no_cycles =
            slices().matching("..(*)..")
                    .should().beFreeOfCycles()
                    .because("For maintainability reasons")
            ;
}
