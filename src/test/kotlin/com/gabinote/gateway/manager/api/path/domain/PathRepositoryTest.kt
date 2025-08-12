package com.gabinote.gateway.manager.api.path.domain

import com.gabinote.gateway.manager.api.item.domain.Item
import com.gabinote.gateway.manager.api.testSupport.testTemplate.RepositoryTestTemplate
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull

class PathRepositoryTest(
    private val pathRepository: PathRepository,
) : RepositoryTestTemplate() {
    init {
        describe("[Path] PathRepository") {
            describe("PathRepository.findByIdOrNull") {
                context("올바른 ID로 Path를 조회할 때") {
                    dbUnitTestHelper.loadDataSets(
                        "dataset/item/domain/base-item.xml",
                        "dataset/path/domain/base-path.xml"
                    )
                    val validPathId = 1L

                    val result = pathRepository.findByIdOrNull(validPathId)
                    it("해당 id를 가진 Path를 반환한다") {
                        result shouldNotBe null
                        result?.id shouldBe validPathId
                    }
                }

                context("존재하지 않는 ID로 Path를 조회할 때") {
                    dbUnitTestHelper.loadDataSets(
                        "dataset/item/domain/base-item.xml",
                        "dataset/path/domain/base-path.xml"
                    )
                    val invalidPathId = 999L

                    val result = pathRepository.findByIdOrNull(invalidPathId)
                    it("null을 반환한다") {
                        result shouldBe null
                    }
                }
            }

            describe("PathRepository.findAllByItem") {
                context("Path를 가진 Item 으로 조회할 때") {
                    dbUnitTestHelper.loadDataSets(
                        "dataset/item/domain/base-item.xml",
                        "dataset/path/domain/base-path.xml"
                    )
                    val validItem = testEntityManager.find(Item::class.java, 1L)

                    val result = pathRepository.findAllByItem(validItem, PageRequest.ofSize(2))
                    it("해당 Item에 연결된 모든 Path를 반환한다") {
                        result shouldNotBe null
                        result.content.size shouldBe 2
                        result.content[0].item.id shouldBe 1L
                        result.content[1].item.id shouldBe 1L
                    }
                }
                context("Path를 가지지 않은 Item으로 조회할 때") {
                    dbUnitTestHelper.loadDataSets(
                        "dataset/item/domain/base-item.xml",
                        "dataset/path/domain/base-path.xml"
                    )
                    val invalidItem = testEntityManager.find(Item::class.java, 2L)

                    val result = pathRepository.findAllByItem(invalidItem, PageRequest.ofSize(2))
                    it("빈 리스트를 반환한다") {
                        result shouldNotBe null
                        result.content.size shouldBe 0
                    }
                }
                data class PathSortKeyTestCase(
                    val sortKey: String,
                    val direction: Sort.Direction,
                    val expected: List<Long>
                )

                val testCases = listOf(
                    PathSortKeyTestCase(
                        sortKey = "id",
                        direction = Sort.Direction.ASC,
                        expected = listOf(1L, 2L)
                    ),
                    PathSortKeyTestCase(
                        sortKey = "id",
                        direction = Sort.Direction.DESC,
                        expected = listOf(2L, 1L)
                    ),
                    PathSortKeyTestCase(
                        sortKey = "role",
                        direction = Sort.Direction.ASC,
                        expected = listOf(1L, 2L)
                    ),
                    PathSortKeyTestCase(
                        sortKey = "role",
                        direction = Sort.Direction.DESC,
                        expected = listOf(2L, 1L)
                    ),
                    PathSortKeyTestCase(
                        sortKey = "priority",
                        direction = Sort.Direction.ASC,
                        expected = listOf(2L, 1L)
                    ),
                    PathSortKeyTestCase(
                        sortKey = "priority",
                        direction = Sort.Direction.DESC,
                        expected = listOf(1L, 2L)
                    )
                )

                describe("올바른 SortKey가 주어지면, 해당 SortKey로 정렬된 Path 목록을 반환한다") {
                    dbUnitTestHelper.loadDataSets(
                        "dataset/item/domain/base-item.xml",
                        "dataset/path/domain/base-path.xml"
                    )
                    testCases.forEach { testCase ->
                        context("SortKey: ${testCase.sortKey}, Direction: ${testCase.direction} 가 주어진 경우") {
                            val validItem = testEntityManager.find(Item::class.java, 1L)

                            val result = pathRepository.findAllByItem(
                                validItem,
                                PageRequest.of(0, 10, Sort.by(testCase.direction, testCase.sortKey))
                            )

                            it("정렬된 Path 목록을 반환한다") {
                                result.content.size shouldBe 2
                                result.content.map { it.id } shouldBe testCase.expected
                            }
                        }
                    }

                }
            }

            describe("PathRepository.save(신규)") {
                context("올바른 Path를 저장할 때") {
                    dbUnitTestHelper.loadDataSets(
                        "dataset/item/domain/base-item.xml",
                        "dataset/path/domain/base-path.xml"
                    )
                    val item = testEntityManager.find(Item::class.java, 1L)
                    val newPath = Path(
                        item = item,
                        path = "/api/v3/test",
                        enableAuth = true,
                        role = "ROLE_ADMIN",
                        httpMethod = HttpMethodType.POST,
                        priority = 0
                    )

                    val savedPath = pathRepository.save(newPath)
                    pathRepository.flush()
                    it("성공적으로 저장된다") {
                        savedPath.id shouldBe 3L
                        dbUnitTestHelper.assertDataset("dataset/path/domain/save-expected-path.xml")
                    }
                }
            }

            describe("PathRepository.save(수정)") {
                context("올바른 Path를 수정할 때") {
                    dbUnitTestHelper.loadDataSets(
                        "dataset/item/domain/base-item.xml",
                        "dataset/path/domain/base-path.xml"
                    )
                    val existingPath = testEntityManager.find(Path::class.java, 1L)
                    existingPath.path = "/api/v3/updated-test"
                    existingPath.enableAuth = false
                    existingPath.role = "ROLE_USER"
                    existingPath.httpMethod = HttpMethodType.GET
                    existingPath.priority = 1

                    val updatedPath = pathRepository.save(existingPath)
                    pathRepository.flush()
                    it("성공적으로 수정된다") {
                        updatedPath.id shouldBe 1L
                        dbUnitTestHelper.assertDataset("dataset/path/domain/update-expected-path.xml")
                    }
                }
            }

            describe("PathRepository.delete") {
                context("올바른 Path를 삭제할 때") {
                    dbUnitTestHelper.loadDataSets(
                        "dataset/item/domain/base-item.xml",
                        "dataset/path/domain/base-path.xml"
                    )
                    val pathToDelete = testEntityManager.find(Path::class.java, 1L)

                    pathRepository.delete(pathToDelete)
                    pathRepository.flush()
                    it("성공적으로 삭제된다") {
                        val deletedPath = pathRepository.findByIdOrNull(1L)
                        deletedPath shouldBe null
                        dbUnitTestHelper.assertDataset("dataset/path/domain/delete-expected-path.xml")
                    }
                }
            }
        }
    }
}