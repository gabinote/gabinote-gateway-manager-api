package com.gabinote.gateway.manager.api.item.domain

import com.gabinote.gateway.manager.api.testSupport.testTemplate.RepositoryTestTemplate
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull

class ItemRepositoryTest(
    private val itemRepository: ItemRepository,
) : RepositoryTestTemplate() {
    init {
        describe("[Item] ItemRepository") {
            describe("ItemRepository.findByIdOrNull") {
                context("존재하는 올바른 Item 의 ID를 전달한 경우") {
                    val validItemId = 1L
                    dbUnitTestHelper.loadDataSet("dataset/item/domain/base-item.xml")
                    val item = itemRepository.findByIdOrNull(validItemId)
                    it("해당 Item을 반환해야 한다") {
                        item shouldNotBe null
                        item?.id shouldBe validItemId
                    }
                }
                context("존재하지 않는 Item 의 ID를 전달한 경우") {
                    val invalidItemId = 999L
                    dbUnitTestHelper.loadDataSet("dataset/item/domain/base-item.xml")
                    val item = itemRepository.findByIdOrNull(invalidItemId)
                    it("null을 반환해야 한다") {
                        item shouldBe null
                    }
                }
            }
            describe("ItemRepository.findAll(pageable)") {

                data class ItemSortKeyTestCase(
                    val sortKey: String,
                    val direction: Sort.Direction,
                    val expected: List<Long>
                )

                describe("올바른 SortKey가 주어지면, 해당 SortKey로 정렬된 Item 목록을 반환한다") {
                    dbUnitTestHelper.loadDataSet("dataset/item/domain/base-item.xml")
                    val testCases = listOf(
                        ItemSortKeyTestCase(
                            sortKey = "id",
                            direction = Sort.Direction.ASC,
                            expected = listOf(1L, 2L)
                        ),
                        ItemSortKeyTestCase(
                            sortKey = "id",
                            direction = Sort.Direction.DESC,
                            expected = listOf(2L, 1L)
                        ),
                        ItemSortKeyTestCase(
                            sortKey = "name",
                            direction = Sort.Direction.ASC,
                            expected = listOf(1L, 2L) // "test" < "test2"
                        ),
                        ItemSortKeyTestCase(
                            sortKey = "name",
                            direction = Sort.Direction.DESC,
                            expected = listOf(2L, 1L)
                        ),
                        ItemSortKeyTestCase(
                            sortKey = "url",
                            direction = Sort.Direction.ASC,
                            expected = listOf(1L, 2L) // "http://test.com" < "http://test2.com"
                        ),
                        ItemSortKeyTestCase(
                            sortKey = "url",
                            direction = Sort.Direction.DESC,
                            expected = listOf(2L, 1L)
                        ),
                        ItemSortKeyTestCase(
                            sortKey = "prefix",
                            direction = Sort.Direction.ASC,
                            expected = listOf(1L, 2L) // "test1" < "test2"
                        ),
                        ItemSortKeyTestCase(
                            sortKey = "prefix",
                            direction = Sort.Direction.DESC,
                            expected = listOf(2L, 1L)
                        ),
                        ItemSortKeyTestCase(
                            sortKey = "port",
                            direction = Sort.Direction.ASC,
                            expected = listOf(1L, 2L) // 123 < 456
                        ),
                        ItemSortKeyTestCase(
                            sortKey = "port",
                            direction = Sort.Direction.DESC,
                            expected = listOf(2L, 1L)
                        ),
                    )

                    testCases.forEach { testCase ->
                        context("SortKey: ${testCase.sortKey}, Direction: ${testCase.direction} 가 주어진 경우") {
                            val sort = Sort.by(testCase.direction, testCase.sortKey)
                            val items = itemRepository.findAll(sort)
                            it("정상적으로 정렬된 Item 목록을 반환해야 한다") {
                                items.map { it.id } shouldBe testCase.expected
                            }
                        }
                    }
                }
            }
            describe("ItemRepository.save(신규)") {
                context("새로운 Item을 저장하는 경우") {
                    dbUnitTestHelper.loadDataSet("dataset/item/domain/base-item.xml")
                    val newItem = Item(
                        name = "test3",
                        url = "http://test3.com",
                        prefix = "test3",
                        port = 828
                    )

                    val savedItem = itemRepository.save(newItem)
                    itemRepository.flush()
                    it("정상적으로 저장되어야 한다") {
                        savedItem.id shouldBe 3
                        dbUnitTestHelper.assertDataset(
                            "dataset/item/domain/save-expected-item.xml"
                        )
                    }
                }
            }
            describe("ItemRepository.save(수정)") {
                context("기존 Item을 수정하는 경우") {
                    dbUnitTestHelper.loadDataSet("dataset/item/domain/base-item.xml")
                    val existingItem = testEntityManager.find(Item::class.java, 2L)
                    existingItem.changeName("new")
                    existingItem.changeUrl("http://new.com")
                    existingItem.changePrefix("new")
                    existingItem.changePort(828)

                    val updatedItem = itemRepository.save(existingItem)
                    itemRepository.flush()
                    it("정상적으로 수정되어야 한다") {
                        updatedItem.id shouldBe 2L
                        dbUnitTestHelper.assertDataset(
                            "dataset/item/domain/update-expected-item.xml"
                        )
                    }
                }
            }

            describe("ItemRepository.delete") {
                context("존재하는 Item을 삭제하는 경우") {
                    dbUnitTestHelper.loadDataSet("dataset/item/domain/base-item.xml")
                    val itemToDelete = testEntityManager.find(Item::class.java, 2L)
                    itemRepository.delete(itemToDelete)
                    itemRepository.flush()
                    it("해당 Item이 삭제되어야 한다") {
                        dbUnitTestHelper.assertDataset(
                            "dataset/item/domain/delete-expected-item.xml"
                        )
                    }
                }
            }
        }
    }
}