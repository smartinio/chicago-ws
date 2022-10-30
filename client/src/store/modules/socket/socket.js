import { SET_SOCKET, SET_STATUS_CONNECTING, SET_STATUS_CONNECTED, SET_STATUS_FAILED } from './mutation_types'
import { CONNECT, HANDLE_MESSAGE, SEND_ACTION, DISCONNECTED } from './action_types'
import { KICKED, FATAL_ERROR, KEY_ERROR, NICKNAME_ERROR, GAME_ERROR, JSON_ERROR, SNAPSHOT, CURRENTLY_IN_GAME } from '@/dto/message/types'
import { SET_KEY_ERROR, SET_NICKNAME_ERROR } from './../errors/mutation_types'
import { HANDLE_GAME_ERROR, HANDLE_JSON_ERROR, HANDLE_FATAL_ERROR } from './../errors/action_types'
import { HANDLE_CURRENTLY_IN_GAME, HANDLE_SNAPSHOT } from './../game/action_types'
import Message from '@/dto/message/Message'
import Action from '@/dto/action/Action'
import { PING } from '@/dto/action/types'

const state = {
  connected: false,
  status: 'connecting',
  socket: undefined
}

const mutations = {
  [SET_STATUS_CONNECTED] (state) {
    state.status = 'connected'
    state.connected = true
  },
  [SET_STATUS_FAILED] (state) {
    state.status = 'failed'
    state.connected = false
  },
  [SET_STATUS_CONNECTING] (state) {
    state.status = 'connecting'
    state.connected = false
  },
  [SET_SOCKET] (state, socket) {
    state.socket = socket
  }
}

const actions = {
  [CONNECT] ({commit, dispatch}) {
    if (state.socket) {
      state.socket.close()
    }

    commit(SET_STATUS_CONNECTING)
    const socket = new WebSocket(process.env.WS_URL)
    let keepalive

    socket.onopen = () => {
      const ping = JSON.stringify(new Action(PING, 'pong'))
      keepalive = setInterval(() => socket.send(ping), 10000)
      commit(SET_STATUS_CONNECTED)
    }

    const failed = () => {
      clearInterval(keepalive)
      commit(SET_STATUS_FAILED)
      dispatch(DISCONNECTED)
    }

    socket.onclose = failed
    socket.onerror = failed

    socket.onmessage = (event) => {
      dispatch(HANDLE_MESSAGE, event)
    }

    commit(SET_SOCKET, socket)
  },
  [HANDLE_MESSAGE] ({commit, dispatch}, event) {
    const message = new Message(event)
    switch (message.type) {
      case CURRENTLY_IN_GAME:
        dispatch(HANDLE_CURRENTLY_IN_GAME, message.body)
        break
      case KICKED:
        localStorage.removeItem('invitationKey')
        localStorage.removeItem('playerId')
        alert('You were kicked from the game')
        window.location.href = '/'
        break
      case KEY_ERROR:
        commit(SET_KEY_ERROR, message.body)
        break
      case NICKNAME_ERROR:
        commit(SET_NICKNAME_ERROR, message.body)
        break
      case GAME_ERROR:
        dispatch(HANDLE_GAME_ERROR, message.body)
        break
      case JSON_ERROR:
        dispatch(HANDLE_JSON_ERROR, message.body)
        break
      case FATAL_ERROR:
        dispatch(HANDLE_FATAL_ERROR, message.body)
        break
      case SNAPSHOT:
        dispatch(HANDLE_SNAPSHOT, message.body)
        break
      default:
        break
    }
  },
  [SEND_ACTION] ({state}, actionDTO) {
    state.socket.send(JSON.stringify(actionDTO))
  }
}

export default { state, actions, mutations }
