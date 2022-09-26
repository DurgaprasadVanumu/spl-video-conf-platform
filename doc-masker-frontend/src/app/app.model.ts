export type coordinates = {
     "startX": number,
     "startY": number,
     "width": number,
     "height": number
}

export type maskPostData = {
     "pageNumber": number,
     "coordinatesList": coordinates[]

}

export type PageImageData = {
     "base64": string,
     "xratio": number,
     "yratio": number,
}
