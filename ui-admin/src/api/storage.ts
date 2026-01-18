import request from './request'

export type FileType = 'image' | 'video' | 'document' | 'code' | 'archive' | 'audio' | 'avatar' | 'file'

export interface UploadResult {
  fileName: string
  url: string
  size: number
  contentType: string
}

/**
 * 上传文件
 */
export const uploadFile = (file: File, type: FileType): Promise<UploadResult> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)
  return request.post<any, UploadResult>('/storage/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传头像
 */
export const uploadAvatar = (file: File): Promise<UploadResult> => {
  return uploadFile(file, 'avatar')
}
