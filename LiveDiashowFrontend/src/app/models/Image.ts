export interface Image {
  id: string;
  data: string;
  creationDate: Date;
  excluded: boolean;
}

export type ImageSummary = Pick<Image, 'id' | 'creationDate' | 'excluded'>;
