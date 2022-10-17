import { SET_SOCKET, SET_CONNECTED_TRUE, SET_CONNECTED_FALSE } from './mutation_types'
import { CONNECT, HANDLE_MESSAGE, SEND_ACTION } from './action_types'
import { KEY_ERROR, NICKNAME_ERROR, GAME_ERROR, JSON_ERROR, SNAPSHOT } from '@/dto/message/types'
import { SET_KEY_ERROR, SET_NICKNAME_ERROR } from './../errors/mutation_types'
import { HANDLE_GAME_ERROR, HANDLE_JSON_ERROR } from './../errors/action_types'
import { HANDLE_SNAPSHOT } from './../game/action_types'
import Message from '@/dto/message/Message'
import Action from '@/dto/action/Action'
import { PING } from '@/dto/action/types'

const state = {
  connected: false,
  socket: undefined
}

const mutations = {
  [SET_CONNECTED_TRUE] (state) {
    state.connected = true
  },
  [SET_CONNECTED_FALSE] (state) {
    state.connected = false
  },
  [SET_SOCKET] (state, socket) {
    state.socket = socket
  }
}

const actions = {
  [CONNECT] ({commit, dispatch}) {
    const socket = new WebSocket(process.env.WS_URL)

    let keepalive

    socket.onopen = () => {
      const ping = JSON.stringify(new Action(PING, 'pong'))
      keepalive = setInterval(() => socket.send(ping), 10000)
      commit(SET_CONNECTED_TRUE)
    }

    socket.onclose = () => {
      clearInterval(keepalive)
      commit(SET_CONNECTED_FALSE)
    }

    socket.onmessage = (event) => {
      dispatch(HANDLE_MESSAGE, event)
    }

    commit(SET_SOCKET, socket)
  },
  [HANDLE_MESSAGE] ({commit, dispatch}, event) {
    const message = new Message(event)
    switch (message.type) {
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
