'use strict'

/**
 * 比较2个变量是否相同
 * @method isDifferent
 * @param  {[type]}   o1 [description]
 * @param  {[type]}   o2 [description]
 * @return boolean
 * @author kkt
 * @date   2016-01-25
 */
let isDifferent = (o1, o2) => {
  const type1 = typeof o1
  const type2 = typeof o2

// 比较类型，类型不同直接返回true
  if (type1 !== type2) {
    return true
  }
  switch (type1) {
// 如果都是object则判断是否数组
    case 'object':
      const deepType1 = o1 instanceof Array
      const deepType2 = o2 instanceof Array

      if (deepType1 === deepType2) {
        if (deepType1) {
      // 均为array
      // 数组长度不同直接返回false，否则遍历数组
          if (o1.length !== o2.length) {
            return true
          } else {
            for (let i = 0, j = o1.length; i < j; i++) {
              if (isDifferent(o1[i], o2[i])) {
                return true
              }
            }
          }
        } else {
      // 均为object
      // 遍历对象
          for (let i in o1) {
            if (isDifferent(o1[i], o2[i])) {
              return true
            }
          }
          for (let i in o2) {
            if (isDifferent(o1[i], o2[i])) {
              return true
            }
          }
        }
      } else {
        return true
      }
      break
    default:
      for (let v in o1) {
        if (!o2.hasOwnProperty(v)) {
          return true
        } else {
          if (isDifferent(o1[v], o2[v])) {
            return true
          }
        }
      }
      return true
  }
  return false
}

let isEmpty = (obj) => {
  if (!obj) {
    return true
  } else {
    for (let letiable in obj) {
      return false
    }
    return true
  }
}

let arrIsEmpty = (arr) => {
  return !arr || typeof arr.length === 'undefined' || arr.length <= 0
}

module.exports = {
  isDifferent: isDifferent,
  isEmpty: isEmpty,
  arrIsEmpty: arrIsEmpty
}
