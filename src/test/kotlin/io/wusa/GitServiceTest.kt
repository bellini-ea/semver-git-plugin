package io.wusa

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.wusa.exception.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GitServiceTest {

    private lateinit var project: Project

    @BeforeEach
    internal fun setUp() {
        project = ProjectBuilder.builder().build()
        mockkObject(GitCommandRunner)
    }

    @AfterEach
    internal fun tearDown() {
        unmockkObject(GitCommandRunner)
    }

    @Test
    fun `git is dirty`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns "modified"
        Assertions.assertEquals(true, GitService.isDirty(project))
    }

    @Test
    fun `git is not dirty`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } throws GitException("error")
        Assertions.assertEquals(false, GitService.isDirty(project))
    }

    @Test
    fun `get last tag`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns "modified"
        Assertions.assertEquals("modified", GitService.lastTag(project))
    }

    @Test
    fun `no last tag`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } throws GitException("error")
        Assertions.assertThrows(NoLastTagFoundException::class.java) {
            GitService.lastTag(project)
        }
    }

    @Test
    fun `get current tag`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns "modified"
        Assertions.assertEquals("modified", GitService.currentTag(project))
    }

    @Test
    fun `no current tag`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } throws GitException("error")
        Assertions.assertThrows(NoCurrentTagFoundException::class.java) {
            GitService.currentTag(project)
        }
    }

    @Test
    fun `get commit sha`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns "5f68d6b1ba57fd183e2c0e6cb968c4353907fa17"
        Assertions.assertEquals("5f68d6b1ba57fd183e2c0e6cb968c4353907fa17", GitService.currentCommit(project, false))
    }

    @Test
    fun `no current commit sha`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } throws GitException("error")
        Assertions.assertThrows(NoCurrentCommitFoundException::class.java) {
            GitService.currentCommit(project, false)
        }
    }

    @Test
    fun `get short commit sha`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns "916776"
        Assertions.assertEquals("916776", GitService.currentCommit(project, true))
    }

    @Test
    fun `no current short commit sha`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } throws GitException("error")
        Assertions.assertThrows(NoCurrentCommitFoundException::class.java) {
            GitService.currentCommit(project, true)
        }
    }

    @Test
    fun `get current branch master`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns "* master 5824168c73ba0618c1b6e384fbd7d61c5e8b8bc3"
        Assertions.assertEquals("master", GitService.currentBranch(project))
    }

    @Test
    fun `get current branch feature-test`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns "* feature/test 5824168c73ba0618c1b6e384fbd7d61c5e8b8bc3"
        Assertions.assertEquals("feature/test", GitService.currentBranch(project))
    }

    @Test
    fun `get current branch feature-test with origin`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns
                "* feature/test                cd55642b18ef34d976eda337d2b7abd296b37c8f remove code quality\n" +
                "  remotes/origin/feature/test cd55642b18ef34d976eda337d2b7abd296b37c8f remove code quality"
        Assertions.assertEquals("feature/test", GitService.currentBranch(project))
    }

    @Test
    fun `get current branch feature-reactiveTests with origin`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns
                "* feature/reactiveTests           831965a6c57434276c70c8e1134244dd6077b1fc fix tests with timeout\n" +
                "  hotfix/codePrefix                4ad7116a019553ff19d0e338bf7e602374d72c04 [behind 1] fixed publish code test for added prefix\n" +
                "  remotes/origin/develop           13fc04d392d51d9bc7b70c8d52b2d8cd6cc1199a Merge branch 'feature/reactive-tests' into 'develop'\n" +
                "  remotes/origin/hotfix/codePrefix a3b40aa8be599003c8656d5cc9a460ffd61fe1f9 escaping / in regex for branch detection\n"
        Assertions.assertEquals("feature/reactiveTests", GitService.currentBranch(project))
    }

    @Test
    fun `get current branch hotfix-codePrefix with origin`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns
                "* hotfix/codePrefix                4ad7116a019553ff19d0e338bf7e602374d72c04 [behind 1] fixed publish code test for added prefix\n" +
                "  remotes/origin/hotfix/codePrefix a3b40aa8be599003c8656d5cc9a460ffd61fe1f9 escaping / in regex for branch detection"
        Assertions.assertEquals("hotfix/codePrefix", GitService.currentBranch(project))
    }

    @Test
    fun `get current branch feature-s-version-3 with origin`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns
                "* feature/s-version-3                9ea487bb167c89a6453fd2e72740a492c6782887 use kebab case\n" +
                "  remotes/origin/feature/s-version-3 9ea487bb167c89a6453fd2e72740a492c6782887 use kebab case"
        Assertions.assertEquals("feature/s-version-3", GitService.currentBranch(project))
    }

    @Test
    fun `get current branch feature-abcd-10847-abcde-abc with origin`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns
                "* feature/abcd-10847-abcde-abc                9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc\n" +
                "  remotes/origin/feature/abcd-10847-abcde-abc 9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc"
        Assertions.assertEquals("feature/abcd-10847-abcde-abc", GitService.currentBranch(project))
    }

    @Test
    fun `get current branch hotfix-5-3-1 with origin`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns
                "* hotfix/5.3.1              9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc\n" +
                "  remotes/origin/hotfix/5.3.1 9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc"
        Assertions.assertEquals("hotfix/5.3.1", GitService.currentBranch(project))
    }

    @Test
    fun `issue-35 fix branch regex`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns
                "* feature/bellini/test-branch-version              9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc\n" +
                "  remotes/origin/feature/bellini/test-branch-version 9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc"
        Assertions.assertEquals("feature/bellini/test-branch-version", GitService.currentBranch(project))
    }

    @Test
    fun `camelCase branch`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns
                "* feature/camelCase              9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc\n" +
                "  remotes/origin/feature/camelCase 9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc"
        Assertions.assertEquals("feature/camelCase", GitService.currentBranch(project))
    }

    @Test
    fun `UPPER_CASE branch`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns
                "* feature/UPPER_CASE              9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc\n" +
                "  remotes/origin/feature/UPPER_CASE 9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc"
        Assertions.assertEquals("feature/UPPER_CASE", GitService.currentBranch(project))
    }

    @Test
    fun `PascalCase branch`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns
                "* feature/PascalCase              9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc\n" +
                "  remotes/origin/feature/PascalCase 9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc"
        Assertions.assertEquals("feature/PascalCase", GitService.currentBranch(project))
    }

    @Test
    fun `get current branch with null pointer exception`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } throws KotlinNullPointerException()
        Assertions.assertThrows(NoCurrentBranchFoundException::class.java) {
            GitService.currentBranch(project)
        }
    }

    @Test
    fun `no current branch`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } throws GitException("error")
        Assertions.assertThrows(NoCurrentBranchFoundException::class.java) {
            GitService.currentBranch(project)
        }
    }

    @Test
    fun `get list of all commits since last tag`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns
                "7356414 update gradle plugin publish due to security bugs\n" +
                "45f65f6 update version an changelog\n" +
                "67f03b1 Merge pull request #18 from ilovemilk/feature/support-multi-module\n" +
                "fba5872 add default tagPrefix behaviour\n" +
                "2d03c4b Merge pull request #17 from jgindin/support-multi-module\n" +
                "f96697f Merge remote-tracking branch 'origin/feature/add-more-tests' into develop\n" +
                "73fc8b4 Add support for multi-module projects.\n" +
                "74e3eb1 add test for kebab-case with numbers\n" +
                "63ca60f add more tests"
        val listOfCommits = listOf(
                "7356414 update gradle plugin publish due to security bugs",
                "45f65f6 update version an changelog",
                "67f03b1 Merge pull request #18 from ilovemilk/feature/support-multi-module",
                "fba5872 add default tagPrefix behaviour",
                "2d03c4b Merge pull request #17 from jgindin/support-multi-module",
                "f96697f Merge remote-tracking branch 'origin/feature/add-more-tests' into develop",
                "73fc8b4 Add support for multi-module projects.",
                "74e3eb1 add test for kebab-case with numbers",
                "63ca60f add more tests")
        Assertions.assertEquals(GitService.getCommitsSinceLastTag(project), listOfCommits)
    }
}
