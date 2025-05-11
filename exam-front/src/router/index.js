import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import Login from '../views/Login.vue' 
import { useAuthStore } from '../stores/authStore'
import StudentList from '../views/student/StudentList.vue'
import AdminList from '../views/admin/AdminList.vue'
import ExamPaperList from '../views/exam/ExamPaperList.vue'
import ExamPaperDetail from '../views/exam/ExamPaperDetail.vue'
import ExamHistory from '../views/exam/ExamHistory.vue'
import registerVue from '../views/register.vue'
// 导入试卷管理组件
import ExamPaperManage from '../views/admin/ExamPaperManage.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: registerVue
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',   
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'student/list',
        name: 'studentList',
        component: StudentList
      },
      {
        path: 'admin/list',
        name: 'adminList',
        component: AdminList,
        meta: {
          requireSuperAdmin: true
        }
      },
      {
        path: 'exam/paper',
        name: 'examPaperList',
        component: ExamPaperList,
        meta: {
          requiresAuth: true
        }
      },
      {
        path: 'exam/history2',
        name: 'examHistory2',
        component: ExamHistory,
        meta: {
          requiresAuth: true
        }
      },
      {
        path: 'admin/course',
        name: 'courseManage',
        component: () => import('@/views/admin/CourseManage.vue'),
        meta: {
          requireSuperAdmin: true,
          title: '课程管理'
        }
      },
      {
        path: 'exam/paper/:id',
        name: 'examPaperDetail',
        component: ExamPaperDetail,
        meta: {
          requiresAuth: true
        }
      },
      {
        path: '/exam-room',
        redirect: '/exam-room/list',
        name: 'ExamRoom',
        children: [
          {
            path: 'list',
            name: 'ExamRoomList',
            component: () => import('@/views/exam-room/ExamRoomList.vue'),
            meta: {
              requiresAuth: true
            }
          },
          {
            path: 'detail/:id',
            name: 'ExamRoomDetail',
            component: () => import('@/views/exam-room/ExamRoomDetail.vue'),
            meta: {
              requiresAuth: true
            }
          }
        ]
      },
      {
        path: 'exam/list',
        name: 'examList',
        component: () => import('@/views/exam/ExamList.vue'),
        meta: {
          requiresAuth: true
        }
      },
      {
        path: 'exam/assign-room/:id',
        name: 'assignRoom',
        component: () => import('@/views/exam/AssignRoom.vue'),
        meta: {
          requiresAuth: true
        }
      },
      {
        path: 'profile',
        name: 'studentProfile',
        component: () => import('@/views/student/Profile.vue'),
        meta: {
          requiresAuth: true
        }
      },
      {
        path: 'exam/register',
        name: 'examRegister',
        component: () => import('@/views/student/ExamRegister.vue'),
        meta: {
          requiresAuth: true
        }
      },
      {
        path: 'exam/my',
        name: 'examMy',
        component: () => import('@/views/student/MyExam.vue'),
        meta: {
          requiresAuth: true
        }
      },
      {
        path: 'exam/:id',
        name: 'exam',
        component: () => import('@/views/student/Exam.vue'),
        meta: {
          requiresAuth: true
        }
      },
      {
        path: 'exam/history',
        name: 'examHistory',
        component: () => import('@/views/student/ExamHistory.vue'),
        meta: {
          requiresAuth: true
        }
      },
      {
        path: 'announcement/list',
        name: 'announcementList',
        component: () => import('@/views/announcement/AnnouncementList.vue'),
        meta: {
          requiresAuth: true
        }
      },
      {
        path: 'announcements',
        name: 'studentAnnouncements',
        component: () => import('@/views/student/Announcements.vue'),
        meta: {
          requiresAuth: true
        }
      },
      {
        path: '/discussion',
        redirect: '/discussion/list',
        name: 'Discussion',
        meta: { title: '讨论', icon: 'el-icon-chat-line-round' },
        children: [
          {
            path: 'list',
            name: 'DiscussionList',
            component: () => import('@/views/discussion/DiscussionList.vue'),
            meta: { title: '讨论列表' }
          },
          {
            path: 'detail/:id',
            name: 'DiscussionDetail',
            component: () => import('@/views/discussion/DiscussionDetail.vue'),
            meta: { title: '讨论详情', activeMenu: '/discussion/list' },
            hidden: true
          }
        ]
      },
      {
        path: 'admin/exam-paper',
        name: 'examPaperManage',
        component: ExamPaperManage,
        meta: {
          requiresAuth: true,
          requireSuperAdmin: true,
          title: '试卷管理'
        }
      },
      {
        path: 'admin/question-type',
        name: 'questionTypeManage',
        component: () => import('@/views/admin/QuestionTypeManage.vue'),
        meta: {
          requiresAuth: true,
          requireSuperAdmin: true,
          title: '题型管理'
        }
      },
    ]
  },
  {
    path: '/admin',
    component: MainLayout,
    children: [
      {
        path: 'course',
        name: 'CourseManage',
        component: () => import('@/views/admin/CourseManage.vue'),
        meta: { title: '课程管理', icon: 'education' }
      }
    ]
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL), // 使用 History 模式
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore() 
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')
  if (to.name === 'Register') {
    return next()
  }

  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!authStore.isLoggedIn) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    } else {
      next()
    }
  } else {
    if (to.name === 'Login' && authStore.isLoggedIn) {
        next({ path: '/dashboard' });
    } else {
      if (!token && to.name !== 'Login') {
        next('/login')
      } else if (to.meta.requireSuperAdmin && userRole !== 'SUPER_ADMIN') {
        next('/')
      } else {
        next()
      }
    }
  }
})

export default router 